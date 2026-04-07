package com.goodee.beedan.controller.payment;

import com.goodee.beedan.common.constant.PaymentMethod;
import com.goodee.beedan.config.security.MemberUserDetails;
import com.goodee.beedan.entity.*;
import com.goodee.beedan.repository.member.MemberRepository;
import com.goodee.beedan.repository.payment.PaymentRepository;
import com.goodee.beedan.repository.quote.QuoteDetailRepository;
import com.goodee.beedan.repository.quote.QuoteInfoRepository;
import com.goodee.beedan.service.order.OrderService;
import com.goodee.beedan.service.quote.QuoteBaseService;
import com.goodee.beedan.service.quote.QuoteDetailService;
import com.goodee.beedan.service.quote.QuoteShipFeeService;
import com.goodee.beedan.service.quote.NegotiationService;
import com.goodee.beedan.service.stock.StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
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

    private final OrderService orderService;

    @Value("${toss.payments.secret-key}")
    private String tossSecretKey;

    @GetMapping("/check")
    public String getCheck(@RequestParam Long quId, Model model,
                           @AuthenticationPrincipal MemberUserDetails userDetails) {
        if (userDetails == null) return "redirect:/auth/signin";

        QuoteBase quoteBase = quoteBaseService.findById(quId);
        QuoteInfo quoteInfo = quoteInfoRepository.findByQuId(quId).orElse(null);
        List<QuoteDetail> details = quoteDetailService.findAllByQuote(quId);
        Negotiation negotiation = negotiationService.findById(quoteBase.getNgId());

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
        if (userDetails == null) return "redirect:/auth/signin";

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
        QuoteBase quoteBase = quoteBaseService.findById(quId);
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

        // 재고 히트 기록
        List<QuoteDetail> details = quoteDetailService.findAllByQuote(quId);
        for(QuoteDetail d : details) {
            stockService.stockHitRecord(d.getStId());
        }

        log.info("결제 완료. paymentId: {}, quId: {}, amount: {}", payment.getPyId(), quId, amount);



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
        if (userDetails == null) return "redirect:/auth/signin";

        QuoteBase quoteBase = quoteBaseService.findById(quId);
        QuoteInfo quoteInfo = quoteInfoRepository.findByQuId(quId).orElse(null);
        List<QuoteDetail> details = quoteDetailService.findAllByQuote(quId);
        Negotiation negotiation = negotiationService.findById(quoteBase.getNgId());
        Member member = memberRepository.findById(userDetails.getMemberId()).orElse(null);

        // 품목 소계
        BigDecimal itemTotalKrw = BigDecimal.ZERO;
        for (QuoteDetail d : details) {
            if (d.getQuDtPr() != null) itemTotalKrw = itemTotalKrw.add(d.getQuDtPr());
        }

        // 결제 정보 조회
        Payment payment = paymentRepository.findByQuId(quId).orElse(null);

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
    public String getFail() {
        return "/payment/payment-fail";
    }

}
