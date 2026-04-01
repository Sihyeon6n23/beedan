package com.goodee.beedan.controller.quote;

import com.goodee.beedan.config.security.MemberUserDetails;
import com.goodee.beedan.dto.quote.CartToQuoteDto;
import com.goodee.beedan.dto.quote.NegotiationRequest;
import com.goodee.beedan.dto.quote.QuoteBaseRequest;
import com.goodee.beedan.dto.quote.QuoteRequestDto;
import com.goodee.beedan.entity.*;
import com.goodee.beedan.repository.member.MemberRepository;
import com.goodee.beedan.repository.quote.HsCodeRepository;
import com.goodee.beedan.repository.quote.QuoteInfoRepository;
import com.goodee.beedan.repository.quote.ShippingInsuranceRepository;
import com.goodee.beedan.repository.quote.StockInspectionRepository;
import com.goodee.beedan.repository.receiver.ReceiverRepository;
import com.goodee.beedan.repository.stock.StockRepository;
import com.goodee.beedan.service.exchangeRate.ExchangeRateService;
import com.goodee.beedan.service.quote.*;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/quote")
@RequiredArgsConstructor
public class QuoteController {

    private final NegotiationService negotiationService;
    private final QuoteBaseService quoteBaseService;
    private final StockRepository stockRepository;
    private final HsCodeRepository hsCodeRepository;
    private final UnitGroupService unitGroupService;
    private final ExchangeRateService exchangeRateService;
    private final ShippingInsuranceRepository shippingInsuranceRepository;
    private final StockInspectionRepository stockInspectionRepository;
    private final DomesticDeliveryRateService domesticDeliveryRateService;
    private final QuoteDetailService quoteDetailService;
    private final QuoteInfoRepository quoteInfoRepository;
    private final ReceiverRepository receiverRepository;
    private final MemberRepository memberRepository;

    @PostMapping("/request")
    @ResponseBody
    public ResponseEntity<Map<String, String>> postItems(
            @RequestBody @Valid QuoteRequestDto dto,
            @AuthenticationPrincipal MemberUserDetails userDetails,
            HttpSession session) {

        Long memId = userDetails.getMemberId();

        // 1. Negotiation 생성
        long millis = System.currentTimeMillis() % 10000; // 0 ~ 9999
        String ngNm = "QU" + String.format("%04d", millis);
        Negotiation negotiation = negotiationService.create(
                NegotiationRequest.builder()
                        .ngNm(ngNm)
                        .memId(memId)
                        .build()
        );

        // 2. QuoteBase 생성
        // quRid = 작성자 = 클라이언트 (초기 견적 요청서 작성자)
        // quSid = 수신자 = 클라이언트 (큐레이터가 응답 시 새 QuoteBase를 생성하며 각자의 id 사용)
        QuoteBase quoteBase = quoteBaseService.create(
                QuoteBaseRequest.builder()
                        .ngId(negotiation.getNgId())
                        .quRid(memId)
                        .build()
        );

        // 3. 세션에 견적 항목 저장 (getWrite에서 사용)
        session.setAttribute("quoteItems_" + quoteBase.getQuId(), dto.getItems());

        return ResponseEntity.ok(Map.of("redirectUrl", "/quote/write?quId=" + quoteBase.getQuId()));
    }

    @GetMapping("/list")
    public String getList() {
        return "/quote/quote-list";
    }

    @SuppressWarnings("unchecked")
    @GetMapping("/write")
    public String getWrite(@RequestParam(required = false) Long quId,
                           Model model, HttpSession session,
                           @AuthenticationPrincipal MemberUserDetails userDetails) {

        // quId 없으면 메인으로 리다이렉트
        if (quId == null) {
            return "redirect:/mainPage";
        }

        // quId 조회 후 TEMP_SAVE 상태가 아니면 406
        QuoteBase quoteBase = quoteBaseService.findById(quId);
        if (quoteBase == null) {
            return "redirect:/mainPage";
        }
        if (!quoteBase.isEditable()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.NOT_ACCEPTABLE,
                    "임시저장 상태의 견적만 수정할 수 있습니다."
            );
        }

        model.addAttribute("activeStep", 1);

        // 환율 데이터 로딩
        List<ExchangeRate> exchangeRates = exchangeRateService.findAllLatest();
        model.addAttribute("exchangeRates", exchangeRates);

        List<UnitGroup> unitGroups = unitGroupService.findAllActive();
        UnitGroup defaultUnit = unitGroups.isEmpty() ? null : unitGroups.get(0);
        model.addAttribute("unitGroups", unitGroups);
        model.addAttribute("quId", quId);

        // 견적 코드 (ngNm) - quoteBase는 위에서 이미 조회됨
        Negotiation negotiation = negotiationService.findById(quoteBase.getNgId());
        model.addAttribute("ngNm", negotiation.getNgNm());

        // 1) 세션에서 신규 견적 데이터 확인
        List<QuoteRequestDto.QuoteRequestItemDto> sessionItems =
                (List<QuoteRequestDto.QuoteRequestItemDto>) session.getAttribute("quoteItems_" + quId);

        // 2) DB에서 임시저장 데이터 확인
        List<QuoteDetail> savedDetails = quoteDetailService.findAllByQuote(quId);
        QuoteInfo savedInfo = quoteInfoRepository.findByQuId(quId).orElse(null);

        if (savedDetails != null && !savedDetails.isEmpty()) {
            // ── 임시저장 복원 ──
            List<CartToQuoteDto.Item> quoteItems = new ArrayList<>();
            for (int i = 0; i < savedDetails.size(); i++) {
                QuoteDetail detail = savedDetails.get(i);
                Stock stock = detail.getStId() != null
                        ? stockRepository.findById(detail.getStId()).orElse(null) : null;
                if (stock == null) continue;
                HsCode hsCode = stock.getCatId() != null
                        ? hsCodeRepository.findByCatId(stock.getCatId()).orElse(null) : null;
                quoteItems.add(CartToQuoteDto.Item.fromDraft(i + 1, stock, detail, defaultUnit, hsCode));
            }
            model.addAttribute("cartToQuote", CartToQuoteDto.builder().items(quoteItems).build());

            // 저장된 메모, 보험/검사 선택
            if (savedInfo != null) {
                model.addAttribute("draftMemo", savedInfo.getQuInfoPs());
                model.addAttribute("draftSiId", savedInfo.getSiId());
                model.addAttribute("draftStiId", savedInfo.getStiId());
            }

        } else if (sessionItems != null && !sessionItems.isEmpty()) {
            // ── 신규 견적 (세션에서) ──
            List<CartToQuoteDto.Item> quoteItems = new ArrayList<>();
            for (int i = 0; i < sessionItems.size(); i++) {
                QuoteRequestDto.QuoteRequestItemDto item = sessionItems.get(i);
                Stock stock = stockRepository.findById(item.getStId()).orElse(null);
                if (stock == null) continue;
                HsCode hsCode = stock.getCatId() != null
                        ? hsCodeRepository.findByCatId(stock.getCatId()).orElse(null) : null;
                quoteItems.add(CartToQuoteDto.Item.of(i + 1, stock, item.getQty(), defaultUnit, hsCode));
            }
            model.addAttribute("cartToQuote", CartToQuoteDto.builder().items(quoteItems).build());
        }

        // 부가 서비스 옵션
        model.addAttribute("insurances", shippingInsuranceRepository.findAllBySiYnTrue());
        model.addAttribute("inspections", stockInspectionRepository.findAllByStiYnTrue());

        // 국내 배송 지역 목록
        model.addAttribute("domesticRates", domesticDeliveryRateService.findAllActive());

        // 로그인 사용자의 배송지 목록 + 기본 배송지
        if (userDetails != null) {
            try {
                Member member = memberRepository.findByMemLgnId(userDetails.getUsername()).orElse(null);
                if (member != null) {
                    List<Receiver> receivers = receiverRepository.findByMember_memIdAndRcDelYnFalseOrderByRcAdrDfYnDescRcIdAsc(member.getMemId());
                    model.addAttribute("receivers", receivers);
                    Receiver defaultReceiver = receiverRepository.findFirstByMember_memIdAndRcAdrDfYnTrueAndRcDelYnFalse(member.getMemId());
                    if (defaultReceiver == null && !receivers.isEmpty()) defaultReceiver = receivers.get(0);
                    model.addAttribute("defaultReceiver", defaultReceiver);
                }
            } catch (Exception ignored) {}
        }

        return "/quote/quote-write";
    }

    @GetMapping("/detail")
    public String getDetail(Model model) {
        model.addAttribute("activeStep", 3);
        return "/quote/quote-detail";
    }

}
