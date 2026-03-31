package com.goodee.beedan.controller.quote;

import com.goodee.beedan.config.security.MemberUserDetails;
import com.goodee.beedan.dto.quote.CartToQuoteDto;
import com.goodee.beedan.dto.quote.NegotiationRequest;
import com.goodee.beedan.dto.quote.QuoteBaseRequest;
import com.goodee.beedan.dto.quote.QuoteRequestDto;
import com.goodee.beedan.entity.*;
import com.goodee.beedan.repository.quote.HsCodeRepository;
import com.goodee.beedan.repository.quote.ShippingInsuranceRepository;
import com.goodee.beedan.repository.quote.StockInspectionRepository;
import com.goodee.beedan.repository.stock.StockRepository;
import com.goodee.beedan.service.exchangeRate.ExchangeRateService;
import com.goodee.beedan.service.quote.NegotiationService;
import com.goodee.beedan.service.quote.QuoteBaseService;
import com.goodee.beedan.service.quote.UnitGroupService;
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
    private final com.goodee.beedan.service.quote.DomesticDeliveryRateService domesticDeliveryRateService;
    private final com.goodee.beedan.repository.receiver.ReceiverRepository receiverRepository;
    private final com.goodee.beedan.repository.member.MemberRepository memberRepository;

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
        model.addAttribute("activeStep", 1);

        // 환율 데이터 로딩
        List<ExchangeRate> exchangeRates = exchangeRateService.findAllLatest();
        model.addAttribute("exchangeRates", exchangeRates);

        if (quId != null) {
            // 상품 목록 가져오고
            List<QuoteRequestDto.QuoteRequestItemDto> items =
                    (List<QuoteRequestDto.QuoteRequestItemDto>) session.getAttribute("quoteItems_" + quId);

            if (items != null && !items.isEmpty()) {
                // 묶음 찾기 (다스)
                List<UnitGroup> unitGroups = unitGroupService.findAllActive();
                UnitGroup defaultUnit = unitGroups.isEmpty() ? null : unitGroups.get(0);

                //상품 목록 상세 정보 가져오고
                List<CartToQuoteDto.Item> quoteItems = new ArrayList<>();
                for (int i = 0; i < items.size(); i++) {
                    QuoteRequestDto.QuoteRequestItemDto item = items.get(i);
                    Stock stock = stockRepository.findById(item.getStId()).orElse(null);
                    if (stock == null) continue;
                    // 각 상품의 카테고리 별 HsCode 가져오고
                    HsCode hsCode = stock.getCatId() != null
                            ? hsCodeRepository.findByCatId(stock.getCatId()).orElse(null)
                            : null;
                    quoteItems.add(CartToQuoteDto.Item.of(i + 1, stock, item.getQty(), defaultUnit, hsCode));
                }

                CartToQuoteDto cartToQuote = CartToQuoteDto.builder().items(quoteItems).build();
                model.addAttribute("cartToQuote", cartToQuote);
                model.addAttribute("unitGroups", unitGroups);
                model.addAttribute("quId", quId);

                // 견적 코드 (ngNm)
                QuoteBase quoteBase = quoteBaseService.findById(quId);
                Negotiation negotiation = negotiationService.findById(quoteBase.getNgId());
                model.addAttribute("ngNm", negotiation.getNgNm());
            }
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
