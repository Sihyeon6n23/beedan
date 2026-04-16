package com.goodee.beedan.controller.payment;

import com.goodee.beedan.common.constant.PaymentMethod;
import com.goodee.beedan.common.constant.QuoteStatus;
import com.goodee.beedan.config.security.MemberUserDetails;
import com.goodee.beedan.entity.*;
import com.goodee.beedan.repository.member.MemberRepository;
import com.goodee.beedan.repository.payment.PaymentRepository;
import com.goodee.beedan.repository.quote.QuoteDetailRepository;
import com.goodee.beedan.repository.quote.QuoteInfoRepository;
import com.goodee.beedan.service.buyer.BuyerService;
import com.goodee.beedan.service.order.OrderService;
import com.goodee.beedan.service.quote.NegotiationService;
import com.goodee.beedan.service.quote.QuoteBaseService;
import com.goodee.beedan.service.quote.QuoteDetailService;
import com.goodee.beedan.service.quote.QuoteShipFeeService;
import com.goodee.beedan.service.stock.StockService;
import com.goodee.beedan.service.webhook.OrderWebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Slf4j
@Controller
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final QuoteBaseService quoteBaseService;
    private final QuoteDetailService quoteDetailService;
    private final QuoteInfoRepository quoteInfoRepository;
    private final NegotiationService negotiationService;
    private final MemberRepository memberRepository;
    private final PaymentRepository paymentRepository;
    private final QuoteShipFeeService quoteShipFeeService;
    private final QuoteDetailRepository quoteDetailRepository;
    private final StockService stockService;
    private final BuyerService buyerService;
    private final OrderWebhookService orderWebhookService;
    private final com.goodee.beedan.repository.quote.QuoteBaseRepository quoteBaseRepository;
    private final com.goodee.beedan.service.quote.QuoteNotificationService quoteNotificationService;
    private final OrderService orderService;
    private final com.goodee.beedan.repository.pageview.PageViewRepository pageViewRepository;

    @Value("${toss.payments.secret-key}")
    private String tossSecretKey;

    @GetMapping("/list")
    public String getList(@AuthenticationPrincipal MemberUserDetails userDetails, Model model) {
        // 결제 + 견적코드 + 협상명 조인 조회 (1 쿼리)
        List<Map<String, Object>> paymentList = new java.util.ArrayList<>();
        for (Object[] row : paymentRepository.findAllWithQuoteInfoByMemId(userDetails.getMemberId())) {
            Map<String, Object> item = new java.util.LinkedHashMap<>();
            item.put("payment", (Payment) row[0]);
            item.put("quCd", row[1] != null ? (String) row[1] : "-");
            item.put("ngNm", row[2] != null ? (String) row[2] : "-");
            paymentList.add(item);
        }

        model.addAttribute("payments", paymentList);
        return "/payment/payment-list";
    }

    @GetMapping("/check")
    public String getCheck(@RequestParam Long quId, Model model,
                           @AuthenticationPrincipal MemberUserDetails userDetails) {
        QuoteBase quoteBase = quoteBaseService.findById(quId);
        if (quoteBase == null) {
            throw new NoSuchElementException("해당 견적을 찾을 수 없습니다.");
        }
        Negotiation negotiation = negotiationService.findById(quoteBase.getNgId());
        if (negotiation == null
                || !negotiation.getMemId().equals(userDetails.getMemberId())) {
            throw new AccessDeniedException("해당 견적에 접근할 권한이 없습니다.");
        }
        if (quoteBase.getQuStt() == QuoteStatus.PAID) {
            throw new IllegalStateException("이미 결제가 완료된 견적입니다.");
        }
        if (quoteBase.getQuStt() != QuoteStatus.APPROVED) {
            throw new IllegalStateException("결제를 진행할 수 있는 견적이 아닙니다.");
        }
        QuoteInfo quoteInfo = quoteInfoRepository.findByQuId(quId).orElse(null);
        List<QuoteDetail> details = quoteDetailService.findAllByQuote(quId);

        // 회원 정보 (법인 정보 프리필용)
        Member member = memberRepository.findById(userDetails.getMemberId()).orElse(null);

        // 품목 소계
        BigDecimal itemTotalKrw = BigDecimal.ZERO;
        for (QuoteDetail d : details) {
            if (d.getQuDtPr() != null) itemTotalKrw = itemTotalKrw.add(d.getQuDtPr());
        }

        // 운임/관세 — shipFees에서 직접 계산 (detail과 동일)
        List<QuoteShipFee> shipFees = quoteShipFeeService.findAllByQuote(quId);
        BigDecimal intShipFeeOnly = BigDecimal.ZERO;
        BigDecimal totalDutyVat = BigDecimal.ZERO;
        for (QuoteShipFee sf : shipFees) {
            BigDecimal sfShip = sf.getQsfSrAm() != null ? sf.getQsfSrAm() : BigDecimal.ZERO;
            BigDecimal sfPort = sf.getQsfPrtAm() != null ? sf.getQsfPrtAm() : BigDecimal.ZERO;
            BigDecimal sfCust = sf.getQsfCstAm() != null ? sf.getQsfCstAm() : BigDecimal.ZERO;
            BigDecimal sfHs   = sf.getQsfHsCd() != null ? sf.getQsfHsCd() : BigDecimal.ZERO;
            BigDecimal sfIns  = (sf.getQsfInsYn() != null && sf.getQsfInsYn() && sf.getQsfInsAm() != null)
                    ? sf.getQsfInsAm() : BigDecimal.ZERO;
            intShipFeeOnly = intShipFeeOnly.add(sfShip).add(sfPort).add(sfCust).add(sfHs).add(sfIns);
            BigDecimal sfDuty = sf.getQsfDty() != null ? sf.getQsfDty() : BigDecimal.ZERO;
            BigDecimal sfVat  = sf.getQsfVat() != null ? sf.getQsfVat() : BigDecimal.ZERO;
            totalDutyVat = totalDutyVat.add(sfDuty).add(sfVat);
        }
        BigDecimal domesticFee = quoteInfo != null && quoteInfo.getQuInfoDomShiFe() != null
                ? quoteInfo.getQuInfoDomShiFe() : BigDecimal.ZERO;
        BigDecimal serviceFeeAm = quoteInfo != null && quoteInfo.getQuInfoSrvFeAm() != null
                ? quoteInfo.getQuInfoSrvFeAm() : BigDecimal.ZERO;
        BigDecimal calculatedTotal = itemTotalKrw.add(intShipFeeOnly).add(domesticFee)
                .add(serviceFeeAm).add(totalDutyVat);



        model.addAttribute("activeStep", 4);
        model.addAttribute("quoteBase", quoteBase);
        model.addAttribute("quoteInfo", quoteInfo);
        model.addAttribute("details", details);
        model.addAttribute("negotiation", negotiation);
        model.addAttribute("member", member);
        model.addAttribute("itemTotalKrw", itemTotalKrw);
        model.addAttribute("intShipFeeOnly", intShipFeeOnly);
        model.addAttribute("totalDutyVat", totalDutyVat);
        model.addAttribute("calculatedTotal", calculatedTotal);
        model.addAttribute("quId", quId);


        return "/payment/payment-check";
    }

    @Transactional
    @GetMapping("/success")
    public String paymentSuccess(@RequestParam Long quId,
                                 @RequestParam String paymentKey,
                                 @RequestParam String orderId,
                                 @RequestParam Long amount,
                                 @RequestParam(required = false) String method,
                                 @AuthenticationPrincipal MemberUserDetails userDetails) {
        // 0. 견적 소유자 + 상태 검증 (토스 호출 전)
        QuoteBase quoteBase = quoteBaseService.findById(quId);
        if (quoteBase == null) {
            throw new NoSuchElementException("결제할 견적을 찾을 수 없습니다.");
        }
        Negotiation negotiation = negotiationService.findById(quoteBase.getNgId());
        if (negotiation == null
                || !negotiation.getMemId().equals(userDetails.getMemberId())) {
            throw new AccessDeniedException("해당 견적에 접근할 권한이 없습니다.");
        }
        if (quoteBase.getQuStt() == QuoteStatus.PAID) {
            throw new IllegalStateException("이미 결제가 완료된 견적입니다.");
        }
        if (quoteBase.getQuStt() != QuoteStatus.APPROVED) {
            throw new IllegalStateException("결제를 진행할 수 있는 견적이 아닙니다.");
        }

        // 1. 토스페이먼츠 결제 승인 API 호출
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String encoded = Base64.getEncoder()
                .encodeToString((tossSecretKey + ":").getBytes(StandardCharsets.UTF_8));
        headers.set("Authorization", "Basic " + encoded);

        Map<String, Object> tossBody = Map.of(
                "paymentKey", paymentKey,
                "orderId", orderId,
                "amount", amount
        );

        try {
            HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>(tossBody, headers);
            ResponseEntity<Map> tossResponse = restTemplate.postForEntity(
                    "https://api.tosspayments.com/v1/payments/confirm",
                    httpEntity, Map.class);

            if (!tossResponse.getStatusCode().is2xxSuccessful()) {
                log.error("토스 결제 승인 실패: {}", tossResponse.getBody());
                return "redirect:/payment/fail?quId=" + quId;
            }
        } catch (Exception e) {
            log.error("토스 결제 승인 실패: {}", e.getMessage());
            return "redirect:/payment/fail?quId=" + quId;
        }

        // 2. Payment 엔티티 생성 및 저장
        QuoteInfo quoteInfo = quoteInfoRepository.findByQuId(quId).orElse(null);

        PaymentMethod paymentMethod = "CARD".equalsIgnoreCase(method)
                ? PaymentMethod.CARD : PaymentMethod.VIRTUAL_ACCOUNT;

        Payment payment = Payment.builder()
                .quId(quId)
                .ngId(quoteBase.getNgId())
                .memId(userDetails.getMemberId())
                .paymentMethod(paymentMethod)
                .totalAmount(BigDecimal.valueOf(amount))
                .supplyValue(quoteInfo != null && quoteInfo.getQuInfoTp() != null
                        ? quoteInfo.getQuInfoTp() : BigDecimal.ZERO)
                .pgTransactionId(paymentKey)
                .build();
        payment.confirmPayment(paymentKey);
        paymentRepository.save(payment);

        // 견적 상태를 PAID로 변경
        quoteBase.paid();

        // 협상 종료 체크
        negotiationService.checkAndClose(quoteBase.getNgId(), quoteBaseRepository);

        // 결제 완료 메일 알림
        quoteNotificationService.notifyOnPaid(quoteBase);

        // 재고 히트 기록
        List<QuoteDetail> details = quoteDetailService.findAllByQuote(quId);
        for(QuoteDetail d : details) {
            stockService.stockHitRecord(d.getStId());
        }
        // 고객 거래 실적 누적
        try {
            Member customer = memberRepository.findById(negotiation.getMemId()).orElse(null);
            if (customer != null && customer.getMemBizNo() != null) {
                buyerService.updateAfterPayment(
                        buyerService.findByBizNo(customer.getMemBizNo()).getById(),
                        BigDecimal.valueOf(amount));
            }
        } catch (Exception e) {
            log.warn("고객 실적 업데이트 실패: {}", e.getMessage());
        }

        log.info("결제 완료. paymentId: {}, quId: {}, amount: {}", payment.getPyId(), quId, amount);

        // 외부팀에 주문 정보 전달 (비동기, 실패해도 결제에 영향 없음)
        try {
            orderWebhookService.sendOrderToExternal(quId, payment.getPyId());
        } catch (Exception e) {
            log.warn("외부팀 webhook 전송 중 예외: {}", e.getMessage());
        }

        return "redirect:/payment/quote-detail?quId=" + quId
                + "&paymentKey=" + paymentKey
                + "&orderId=" + orderId;
    }

    @GetMapping("/receipt")
    public String getReceipt() {
        return "/payment/payment-receipt";
    }

    @GetMapping("/quote-detail")
    public String getQuoteDetail(@RequestParam Long quId,
                                 @RequestParam(required = false) String paymentKey,
                                 @RequestParam(required = false) String orderId,
                                 @AuthenticationPrincipal MemberUserDetails userDetails,
                                 Model model) {
        QuoteBase quoteBase = quoteBaseService.findById(quId);
        if (quoteBase == null) {
            throw new NoSuchElementException("해당 견적을 찾을 수 없습니다.");
        }
        Negotiation negotiation = negotiationService.findById(quoteBase.getNgId());
        if (negotiation == null
                || !negotiation.getMemId().equals(userDetails.getMemberId())) {
            throw new AccessDeniedException("해당 견적에 접근할 권한이 없습니다.");
        }
        QuoteInfo quoteInfo = quoteInfoRepository.findByQuId(quId).orElse(null);
        List<QuoteDetail> details = quoteDetailService.findAllByQuote(quId);
        Member member = memberRepository.findById(userDetails.getMemberId()).orElse(null);

        // 품목 소계
        BigDecimal itemTotalKrw = BigDecimal.ZERO;
        for (QuoteDetail d : details) {
            if (d.getQuDtPr() != null) itemTotalKrw = itemTotalKrw.add(d.getQuDtPr());
        }

        // 결제 정보 조회
        Payment payment = paymentRepository.findFirstByQuIdOrderByPyIdDesc(quId).orElse(null);

        model.addAttribute("activeStep", 5);
        model.addAttribute("quoteBase", quoteBase);
        model.addAttribute("quoteInfo", quoteInfo);
        model.addAttribute("details", details);
        model.addAttribute("negotiation", negotiation);
        model.addAttribute("member", member);
        model.addAttribute("itemTotalKrw", itemTotalKrw);
        model.addAttribute("quId", quId);
        model.addAttribute("payment", payment);

        return "/payment/payment-quote-detail";
    }


    @GetMapping("/fail")
    public String getFail(@RequestParam(required = false) Long quId,
                          @RequestParam(required = false) String code,
                          @RequestParam(required = false) String message,
                          Model model) {
        model.addAttribute("quId", quId);
        model.addAttribute("code", code);
        model.addAttribute("message", message);
        return "/payment/payment-fail";
    }

}
