package com.goodee.beedan.controller.admin;

import com.goodee.beedan.dto.quote.CartToQuoteDto;
import com.goodee.beedan.entity.*;
import com.goodee.beedan.repository.buyer.BuyerGradePolicyRepository;
import com.goodee.beedan.repository.member.MemberRepository;
import com.goodee.beedan.repository.quote.HsCodeRepository;
import com.goodee.beedan.repository.quote.QuoteInfoRepository;
import com.goodee.beedan.repository.quote.ShippingInsuranceRepository;
import com.goodee.beedan.repository.quote.StockInspectionRepository;
import com.goodee.beedan.repository.receiver.ReceiverRepository;
import com.goodee.beedan.repository.stock.StockRepository;
import com.goodee.beedan.service.exchangeRate.ExchangeRateService;
import com.goodee.beedan.service.quote.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminQuoteController {

    private final NegotiationService negotiationService;
    private final QuoteBaseService quoteBaseService;
    private final QuoteDetailService quoteDetailService;
    private final QuoteInfoRepository quoteInfoRepository;
    private final MemberRepository memberRepository;
    private final StockRepository stockRepository;
    private final HsCodeRepository hsCodeRepository;
    private final UnitGroupService unitGroupService;
    private final ExchangeRateService exchangeRateService;
    private final ShippingInsuranceRepository shippingInsuranceRepository;
    private final StockInspectionRepository stockInspectionRepository;
    private final DomesticDeliveryRateService domesticDeliveryRateService;
    private final ReceiverRepository receiverRepository;
    private final QuoteShipFeeService quoteShipFeeService;
    private final BuyerGradePolicyRepository buyerGradePolicyRepository;

    @GetMapping("/negotiation/list")
    public String negotiationList(Model model) {
        List<Negotiation> ngList = negotiationService.findAll();

        List<Map<String, Object>> negotiations = new ArrayList<>();
        for (Negotiation ng : ngList) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("ngId", ng.getNgId());
            item.put("ngNm", ng.getNgNm());
            item.put("ongoing", ng.isOngoing());
            item.put("ngCreDt", ng.getNgCreDt());
            item.put("ngEndDt", ng.getNgEndDt());

            // 견적 목록
            List<QuoteBase> quotes = quoteBaseService.findAllByNego(ng.getNgId());
            item.put("quoteCount", quotes.size());

            // 미열람 견적 존재 여부
            boolean hasUnread = quotes.stream().anyMatch(q -> q.getQuAdOpYn() == null || !q.getQuAdOpYn());
            item.put("hasUnread", hasUnread);

            // 미열람 견적 수 및 ID 목록
            List<Long> unreadQuIds = quotes.stream()
                    .filter(q -> q.getQuAdOpYn() == null || !q.getQuAdOpYn())
                    .map(QuoteBase::getQuId)
                    .collect(java.util.stream.Collectors.toList());
            item.put("unreadCount", unreadQuIds.size());
            item.put("unreadQuIds", unreadQuIds.stream()
                    .map(String::valueOf)
                    .collect(java.util.stream.Collectors.joining(",")));

            // 최근 견적 업데이트 시간
            LocalDateTime latestUpdate = quotes.stream()
                    .map(QuoteBase::getQuUpdDt)
                    .filter(java.util.Objects::nonNull)
                    .max(LocalDateTime::compareTo)
                    .orElse(ng.getNgCreDt());
            item.put("latestUpdate", latestUpdate);

            // 회원 정보
            Member member = memberRepository.findById(ng.getMemId()).orElse(null);
            item.put("memNm", member != null ? member.getMemNm() : "-");
            item.put("memBizTtl", member != null ? member.getMemBizTtl() : "-");

            negotiations.add(item);
        }

        model.addAttribute("negotiations", negotiations);
        return "admin/quote/admin-negotiation-list";
    }

    @GetMapping("/negotiation/detail")
    public String negotiationDetail(@RequestParam Long ngId, Model model) {
        Negotiation negotiation = negotiationService.findById(ngId);

        // 회원 정보
        Member member = memberRepository.findById(negotiation.getMemId()).orElse(null);

        // 견적 목록
        List<QuoteBase> quoteList = quoteBaseService.findAllByNego(ngId);

        // 상태별 카운트
        Map<String, Long> statusCounts = new LinkedHashMap<>();
        for (com.goodee.beedan.common.constant.QuoteStatus s : com.goodee.beedan.common.constant.QuoteStatus.values()) {
            statusCounts.put(s.name(), quoteList.stream().filter(q -> q.getQuStt() == s).count());
        }

        // 견적 상세 데이터
        List<Map<String, Object>> quotes = new ArrayList<>();
        int no = 1;
        for (QuoteBase qb : quoteList) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("no", no++);
            item.put("quId", qb.getQuId());
            item.put("quCd", qb.getQuCd());
            item.put("quStt", qb.getQuStt().name());
            item.put("quOpYn", qb.getQuAdOpYn() != null && qb.getQuAdOpYn());
            item.put("quCreDt", qb.getQuCreDt());
            item.put("quUpdDt", qb.getQuUpdDt());

            List<QuoteDetail> details = quoteDetailService.findAllByQuote(qb.getQuId());
            item.put("itemCount", details != null ? details.size() : 0);
            item.put("firstItemName", details != null && !details.isEmpty()
                    ? details.get(0).getStNm() : null);

            quotes.add(item);
        }

        model.addAttribute("negotiation", negotiation);
        model.addAttribute("member", member);
        model.addAttribute("statusCounts", statusCounts);
        model.addAttribute("quotes", quotes);
        model.addAttribute("quoteCount", quoteList.size());
        return "admin/quote/admin-negotiation-detail";
    }

    @GetMapping("/quote/list")
    public String quoteList(@RequestParam(required = false) Long ngId, Model model) {
        // ngId가 있으면 해당 협상의 견적만, 없으면 전체
        List<QuoteBase> quoteList = (ngId != null)
                ? quoteBaseService.findAllByNego(ngId)
                : quoteBaseService.findAll();

        List<Map<String, Object>> quotes = new ArrayList<>();
        for (QuoteBase qb : quoteList) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("quId", qb.getQuId());
            item.put("quCd", qb.getQuCd());
            item.put("quStt", qb.getQuStt().name());
            item.put("quOpYn", qb.getQuAdOpYn() != null && qb.getQuAdOpYn());
            item.put("quCreDt", qb.getQuCreDt());
            item.put("quUpdDt", qb.getQuUpdDt());

            // 협상명
            Negotiation ng = negotiationService.findById(qb.getNgId());
            item.put("ngNm", ng.getNgNm());

            // 회원 정보
            Member member = memberRepository.findById(ng.getMemId()).orElse(null);
            item.put("memNm", member != null ? member.getMemNm() : "-");
            item.put("memBizTtl", member != null ? member.getMemBizTtl() : "-");

            // 품목 정보
            List<QuoteDetail> details = quoteDetailService.findAllByQuote(qb.getQuId());
            item.put("itemCount", details != null ? details.size() : 0);
            item.put("firstItemName", details != null && !details.isEmpty()
                    ? details.get(0).getStNm() : null);

            // 총 금액
            QuoteInfo info = quoteInfoRepository.findByQuId(qb.getQuId()).orElse(null);
            item.put("totalAmount", info != null ? info.getQuInfoTp() : null);

            quotes.add(item);
        }

        model.addAttribute("quotes", quotes);
        model.addAttribute("ngId", ngId);
        return "admin/quote/admin-quote-list";
    }

    @GetMapping("/quote/detail")
    public String quoteDetail(@RequestParam Long quId, Model model) {
        QuoteBase quoteBase = quoteBaseService.findById(quId);
        if (quoteBase == null) return "redirect:/admin/quote/list";

        Negotiation negotiation = negotiationService.findById(quoteBase.getNgId());
        Member member = memberRepository.findById(negotiation.getMemId()).orElse(null);
        List<QuoteDetail> details = quoteDetailService.findAllByQuote(quId);
        QuoteInfo quoteInfo = quoteInfoRepository.findByQuId(quId).orElse(null);
        List<QuoteShipFee> shipFees = quoteShipFeeService.findAllByQuote(quId);

        // 상태 → activeStep
        int activeStep = switch (quoteBase.getQuStt()) {
            case TEMP_SAVE -> 1;
            case SUBMITTED -> 2;
            case APPROVED -> 3;
            case REJECTED -> 2;
            case EXPIRED -> 2;
        };

        // 표시용 부가 데이터
        List<Map<String, Object>> detailExtras = new ArrayList<>();
        for (QuoteDetail d : details) {
            Map<String, Object> extra = new LinkedHashMap<>();
            Stock stock = d.getStId() != null
                    ? stockRepository.findById(d.getStId()).orElse(null) : null;
            extra.put("stCd", stock != null ? stock.getStCd() : null);
            extra.put("stCur", stock != null ? stock.getStCur() : "");
            if (d.getQuUQn() != null && d.getQuUQn() > 0 && d.getQuDtQn() != null) {
                extra.put("spec", (int) Math.ceil((double) d.getQuDtQn() / d.getQuUQn()));
            } else {
                extra.put("spec", null);
            }
            detailExtras.add(extra);
        }

        // 공장별 품목 수 + 품목 총 한화
        Map<Long, Integer> factoryItemCounts = new java.util.HashMap<>();
        BigDecimal itemTotalKrw = BigDecimal.ZERO;
        for (QuoteDetail d : details) {
            if (d.getFaId() != null) {
                factoryItemCounts.merge(d.getFaId(), 1, Integer::sum);
            }
            if (d.getQuDtPr() != null) {
                itemTotalKrw = itemTotalKrw.add(d.getQuDtPr());
            }
        }

        // 적용 등급
        String buyerGrade = "STANDARD";
        if (quoteInfo != null && quoteInfo.getBgpId() != null) {
            buyerGrade = buyerGradePolicyRepository.findById(quoteInfo.getBgpId())
                    .map(BuyerGradePolicy::getBgpGr).orElse("STANDARD");
        }

        model.addAttribute("quoteBase", quoteBase);
        model.addAttribute("negotiation", negotiation);
        model.addAttribute("member", member);
        model.addAttribute("details", details);
        model.addAttribute("detailExtras", detailExtras);
        model.addAttribute("quoteInfo", quoteInfo);
        model.addAttribute("shipFees", shipFees);
        model.addAttribute("factoryItemCounts", factoryItemCounts);
        model.addAttribute("itemTotalKrw", itemTotalKrw);
        model.addAttribute("buyerGrade", buyerGrade);
        model.addAttribute("activeStep", activeStep);
        return "admin/quote/admin-quote-detail";
    }

    @GetMapping("/quote/write")
    public String quoteWrite(@RequestParam Long quId, Model model) {
        QuoteBase quoteBase = quoteBaseService.findById(quId);
        if (quoteBase == null) return "redirect:/admin/quote/list";

        model.addAttribute("activeStep", 1);
        model.addAttribute("quId", quId);
        model.addAttribute("quCd", quoteBase.getQuCd());

        Negotiation negotiation = negotiationService.findById(quoteBase.getNgId());
        model.addAttribute("ngNm", negotiation.getNgNm());

        // 환율
        model.addAttribute("exchangeRates", exchangeRateService.findAllLatest());

        // 규격
        List<UnitGroup> unitGroups = unitGroupService.findAllActive();
        UnitGroup defaultUnit = unitGroups.isEmpty() ? null : unitGroups.get(0);
        model.addAttribute("unitGroups", unitGroups);

        // 견적 상세 복원
        List<QuoteDetail> savedDetails = quoteDetailService.findAllByQuote(quId);
        QuoteInfo savedInfo = quoteInfoRepository.findByQuId(quId).orElse(null);

        if (savedDetails != null && !savedDetails.isEmpty()) {
            LinkedHashMap<Integer, List<QuoteDetail>> groups = new LinkedHashMap<>();
            int autoGrp = -1;
            for (QuoteDetail d : savedDetails) {
                int grp = d.getQuDtGrp() != null ? d.getQuDtGrp() : autoGrp--;
                groups.computeIfAbsent(grp, k -> new ArrayList<>()).add(d);
            }
            List<CartToQuoteDto.Item> quoteItems = new ArrayList<>();
            int no = 1;
            for (List<QuoteDetail> group : groups.values()) {
                QuoteDetail first = group.get(0);
                Stock stock = first.getStId() != null
                        ? stockRepository.findById(first.getStId()).orElse(null) : null;
                if (stock == null) continue;
                HsCode hsCode = stock.getCatId() != null
                        ? hsCodeRepository.findByCatId(stock.getCatId()).orElse(null) : null;
                quoteItems.add(CartToQuoteDto.Item.fromDraftGroup(no++, stock, group, defaultUnit, hsCode));
            }
            model.addAttribute("cartToQuote", CartToQuoteDto.builder().items(quoteItems).build());

            if (savedInfo != null) {
                model.addAttribute("draftMemo", savedInfo.getQuInfoPs());
                model.addAttribute("draftSiId", savedInfo.getSiId());
                model.addAttribute("draftStiId", savedInfo.getStiId());
            }
        }

        // 부가 서비스
        model.addAttribute("insurances", shippingInsuranceRepository.findAllBySiYnTrue());
        model.addAttribute("inspections", stockInspectionRepository.findAllByStiYnTrue());

        // 국내 배송 지역
        model.addAttribute("domesticRates", domesticDeliveryRateService.findAllActive());

        // 견적 요청자의 배송지
        Member member = memberRepository.findById(negotiation.getMemId()).orElse(null);
        if (member != null) {
            List<Receiver> receivers = receiverRepository.findByMember_memIdAndRcDelYnFalseOrderByRcAdrDfYnDescRcIdAsc(member.getMemId());
            model.addAttribute("receivers", receivers);
            Receiver defaultReceiver = receiverRepository.findFirstByMember_memIdAndRcAdrDfYnTrueAndRcDelYnFalse(member.getMemId());
            if (defaultReceiver == null && !receivers.isEmpty()) defaultReceiver = receivers.get(0);
            model.addAttribute("defaultReceiver", defaultReceiver);
        }

        return "admin/quote/admin-quote-write";
    }
}
