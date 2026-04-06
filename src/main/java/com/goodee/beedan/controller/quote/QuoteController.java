package com.goodee.beedan.controller.quote;

import com.goodee.beedan.common.constant.QuoteStatus;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.goodee.beedan.repository.buyer.BuyerGradePolicyRepository;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
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
    private final QuoteShipFeeService quoteShipFeeService;
    private final ReceiverRepository receiverRepository;
    private final MemberRepository memberRepository;
    private final BuyerGradePolicyRepository buyerGradePolicyRepository;

    @PostMapping("/request")
    @ResponseBody
    public ResponseEntity<Map<String, String>> postItems(
            @RequestBody @Valid QuoteRequestDto dto,
            @AuthenticationPrincipal MemberUserDetails userDetails,
            HttpSession session) {

        Long memId = userDetails.getMemberId();

        // 1. Negotiation 생성
        long millis = System.currentTimeMillis() % 10000; // 0 ~ 9999
        String ngNm = "NG" + String.format("%04d", millis);
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
    public String getList(@AuthenticationPrincipal MemberUserDetails userDetails,
                          @RequestParam(defaultValue = "1") int page,
                          Model model) {
        if (userDetails == null) return "redirect:/auth/signin";
        Long memId = userDetails.getMemberId();

        int pageSize = 10;
        Page<QuoteBase> quPage = quoteBaseService.findAllByMember(
                memId, PageRequest.of(page - 1, pageSize));

        // 각 견적에 대한 협상명, 품목 수, 첫 품목명, 총 금액을 조합
        List<Map<String, Object>> quotes = new ArrayList<>();
        for (QuoteBase qb : quPage.getContent()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("quId", qb.getQuId());
            item.put("quStt", qb.getQuStt().name());
            item.put("quOpYn", qb.getQuUsOpYn() != null && qb.getQuUsOpYn());
            item.put("quAdOpYn", qb.getQuAdOpYn() != null && qb.getQuAdOpYn());
            item.put("quCd", qb.getQuCd());
            item.put("quCreDt", qb.getQuCreDt());
            item.put("quUpdDt", qb.getQuUpdDt());

            quotes.add(item);
        }

        int totalPages = quPage.getTotalPages();
        List<String> pageLabels = new ArrayList<>();
        for (int i = 1; i <= totalPages; i++) {
            pageLabels.add(String.format("%02d", i));
        }

        model.addAttribute("quotes", quotes);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageLabels", pageLabels);
        return "/quote/quote-list";
    }

    @SuppressWarnings("unchecked")
    @GetMapping("/write")
    public String getWrite(@RequestParam(required = false) Long quId,
                           @RequestParam(required = false) Long fromQuId,
                           Model model,
                           HttpSession session,
                           @AuthenticationPrincipal MemberUserDetails userDetails) {

        // 재작성 진입: fromQuId → 기존 견적에서 새 견적 생성
        Long sourceQuId = quId; // 데이터를 로드할 원본 quId
        if (fromQuId != null) {
            QuoteBase oldQuote = quoteBaseService.findById(fromQuId);
            if (oldQuote == null) return "redirect:/mainPage";

            // 송신자 = 나, 수신자 = 상대방
            Long myId = userDetails != null ? userDetails.getMemberId() : null;
            Long receiverId = oldQuote.getQuSid() != null ? oldQuote.getQuSid() : oldQuote.getQuRid();
            if (receiverId != null && receiverId.equals(myId)) {
                receiverId = oldQuote.getQuRid() != null ? oldQuote.getQuRid() : oldQuote.getQuSid();
            }
            QuoteBase newQuote = quoteBaseService.create(
                    new com.goodee.beedan.dto.quote.QuoteBaseRequest(
                            oldQuote.getNgId(), myId, receiverId));

            quId = newQuote.getQuId();
            sourceQuId = fromQuId; // 데이터는 기존 견적에서 로드
            model.addAttribute("rejectedReason", oldQuote.getQuCon());
            model.addAttribute("rewriteFromQuId", fromQuId);
            model.addAttribute("rewriteFromQuCd", oldQuote.getQuCd());

        }

        // quId 없으면 메인으로 리다이렉트
        if (quId == null) {
            return "redirect:/mainPage";
        }

        QuoteBase quoteBase = quoteBaseService.findById(quId);
        if (quoteBase == null) {
            return "redirect:/mainPage";
        }
        // 재작성이 아닌 일반 진입일 때만 editable 체크
        if (fromQuId == null && !quoteBase.isEditable()) {
            return "redirect:/quote/detail?quId=" + quId;
        }

        model.addAttribute("activeStep", 1);

        // 환율 데이터 로딩
        List<ExchangeRate> exchangeRates = exchangeRateService.findAllLatest();
        model.addAttribute("exchangeRates", exchangeRates);

        List<UnitGroup> unitGroups = unitGroupService.findAllActive();
        UnitGroup defaultUnit = unitGroups.isEmpty() ? null : unitGroups.get(0);
        model.addAttribute("unitGroups", unitGroups);
        model.addAttribute("quId", quId);

        // 견적 코드 + 협상명
        model.addAttribute("quCd", quoteBase.getQuCd());
        Negotiation negotiation = negotiationService.findById(quoteBase.getNgId());
        model.addAttribute("ngNm", negotiation.getNgNm());

        // 1) 세션에서 신규 견적 데이터 확인
        List<QuoteRequestDto.QuoteRequestItemDto> sessionItems =
                (List<QuoteRequestDto.QuoteRequestItemDto>) session.getAttribute("quoteItems_" + quId);

        // 2) DB에서 데이터 확인 (재작성 시 원본 견적에서 로드)
        List<QuoteDetail> savedDetails = quoteDetailService.findAllByQuote(sourceQuId);
        QuoteInfo savedInfo = quoteInfoRepository.findByQuId(sourceQuId).orElse(null);

        if (savedDetails != null && !savedDetails.isEmpty()) {
            // ── 임시저장 복원 (분할배송 그룹핑) ──
            // quDtGrp으로 그룹핑 — null이면 개별 그룹 취급
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

        // 기존 운임 데이터 로드 (임시저장 복원 / 재작성 모두)
        List<QuoteShipFee> prevShipFees = quoteShipFeeService.findAllByQuote(sourceQuId);
        if (prevShipFees != null && !prevShipFees.isEmpty()) {
            model.addAttribute("prevShipFees", prevShipFees);
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
    public String getDetail(@RequestParam Long quId,
                            @AuthenticationPrincipal MemberUserDetails userDetails,
                            Model model) {
        if (userDetails == null) return "redirect:/auth/signin";

        QuoteBase quoteBase = quoteBaseService.findById(quId);
        Negotiation negotiation = negotiationService.findById(quoteBase.getNgId());
        List<QuoteDetail> details = quoteDetailService.findAllByQuote(quId);
        QuoteInfo quoteInfo = quoteInfoRepository.findByQuId(quId).orElse(null);

        // 공장별 배송비 (quId로 직접 조회)
        List<QuoteShipFee> shipFees = quoteShipFeeService.findAllByQuote(quId);

        // 상태 → activeStep 변환
        int activeStep = switch (quoteBase.getQuStt()) {
            case TEMP_SAVE -> 1;
            case SUBMITTED -> 2;
            case APPROVED -> 3;
            case REJECTED -> 2;
            case EXPIRED -> 2;
            case PAID -> 5;
        };

        // 표시용 부가 데이터 (stCd, stCur, spec)
        List<Map<String, Object>> detailExtras = new ArrayList<>();
        for (QuoteDetail d : details) {
            Map<String, Object> extra = new java.util.LinkedHashMap<>();
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

        // 공장별 품목 수 (feeCard.js 표시용)
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

        // 국제 운임 (관세/부가세 제외) + 관세/부가세 소계 — shipFees에서 직접 계산
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
        // 최종 금액 = 카드 4개의 합 (DB 기준, 항상 일치)
        BigDecimal calculatedTotal = itemTotalKrw.add(intShipFeeOnly).add(domesticFee)
                .add(serviceFeeAm).add(totalDutyVat);

        model.addAttribute("intShipFeeOnly", intShipFeeOnly);
        model.addAttribute("totalDutyVat", totalDutyVat);
        model.addAttribute("calculatedTotal", calculatedTotal);

        // 적용 등급
        String buyerGrade = "STANDARD";
        if (quoteInfo != null && quoteInfo.getBgpId() != null) {
            buyerGrade = buyerGradePolicyRepository.findById(quoteInfo.getBgpId())
                    .map(BuyerGradePolicy::getBgpGr).orElse("STANDARD");
        }

        // 보험/검사 이름 + 검사 금액
        String insuranceName = null;
        String inspectionName = null;
        BigDecimal inspectionAmount = BigDecimal.ZERO;
        if (quoteInfo != null) {
            if (quoteInfo.getSiId() != null) {
                insuranceName = shippingInsuranceRepository.findById(quoteInfo.getSiId())
                        .map(ShippingInsurance::getSiNm).orElse(null);
            }
            if (quoteInfo.getStiId() != null) {
                var inspection = stockInspectionRepository.findById(quoteInfo.getStiId()).orElse(null);
                if (inspection != null) {
                    inspectionName = inspection.getStiNm();
                    inspectionAmount = inspection.getStiAm() != null ? inspection.getStiAm() : BigDecimal.ZERO;
                }
            }
        }

        model.addAttribute("quoteBase", quoteBase);
        model.addAttribute("negotiation", negotiation);
        model.addAttribute("details", details);
        model.addAttribute("detailExtras", detailExtras);
        model.addAttribute("quoteInfo", quoteInfo);
        model.addAttribute("shipFees", shipFees);
        model.addAttribute("factoryItemCounts", factoryItemCounts);
        model.addAttribute("itemTotalKrw", itemTotalKrw);
        model.addAttribute("buyerGrade", buyerGrade);
        model.addAttribute("activeStep", activeStep);
        model.addAttribute("insuranceName", insuranceName);
        model.addAttribute("inspectionName", inspectionName);
        model.addAttribute("inspectionAmount", inspectionAmount);

        // 현재 사용자가 이 견적의 작성자인지 (작성자면 액션 버튼 숨김)
        Long myId = userDetails.getMemberId();
        boolean isSender = myId.equals(quoteBase.getQuSid())
                || (quoteBase.getQuSid() == null && myId.equals(quoteBase.getQuRid()));
        model.addAttribute("isSender", isSender);

        return "admin/quote/admin-quote-detail";
    }
    @GetMapping("/negotiation/list")
    public String getNegotiationList(@AuthenticationPrincipal MemberUserDetails userDetails,
                                     @RequestParam(defaultValue = "1") int page,
                                     Model model) {
        if (userDetails == null) return "redirect:/auth/signin";
        Long memId = userDetails.getMemberId();

        int pageSize = 10;
        Page<Negotiation> ngPage = negotiationService.findAllByMember(
                memId, org.springframework.data.domain.PageRequest.of(page - 1, pageSize));

        List<Map<String, Object>> negotiations = new ArrayList<>();
        for (Negotiation ng : ngPage.getContent()) {
            Map<String, Object> item = new java.util.LinkedHashMap<>();
            item.put("ngId", ng.getNgId());
            item.put("ngNm", ng.getNgNm());
            item.put("ongoing", ng.isOngoing());
            item.put("ngCreDt", ng.getNgCreDt());
            item.put("ngEndDt", ng.getNgEndDt());

            // 해당 협상의 견적 목록
            List<QuoteBase> quotes = quoteBaseService.findAllByNego(ng.getNgId());
            item.put("quoteCount", quotes.size());

            // 미열람 견적 존재 여부
            boolean hasUnread = quotes.stream().anyMatch(q -> q.getQuUsOpYn() == null || !q.getQuUsOpYn());
            item.put("hasUnread", hasUnread);

            // 최신 견적 업데이트 시간 (정렬용)
            LocalDateTime latestUpdate = quotes.stream()
                    .map(QuoteBase::getQuUpdDt)
                    .filter(java.util.Objects::nonNull)
                    .max(LocalDateTime::compareTo)
                    .orElse(ng.getNgCreDt());
            item.put("latestUpdate", latestUpdate);

            negotiations.add(item);
        }

        int totalPages = ngPage.getTotalPages();
        List<String> pageLabels = new ArrayList<>();
        for (int i = 1; i <= totalPages; i++) {
            pageLabels.add(String.format("%02d", i));
        }

        model.addAttribute("negotiations", negotiations);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageLabels", pageLabels);
        return "/quote/negotiation-list";
    }

    @GetMapping("/negotiation/detail")
    public String getNegotiationDetail(@AuthenticationPrincipal MemberUserDetails userDetails,
                                       @RequestParam Long ngId,
                                       Model model) {
        if (userDetails == null) return "redirect:/auth/signin";

        Negotiation negotiation = negotiationService.findById(ngId);
        if (negotiation == null) return "redirect:/quote/negotiation/list";

        // 소유자 검증
        if (!negotiation.getMemId().equals(userDetails.getMemberId())) {
            return "redirect:/quote/negotiation/list";
        }

        // 해당 협상의 견적 목록
        List<QuoteBase> quoteList = quoteBaseService.findAllByNego(ngId);

        // 수신자 본인의 미열람 견적 열람 처리
        Long memId = userDetails.getMemberId();
        for (QuoteBase qb : quoteList) {
            if (memId.equals(qb.getQuRid()) && (qb.getQuUsOpYn() == null || !qb.getQuUsOpYn())) {
                quoteBaseService.userOpen(qb.getQuId());
            }
        }

        // 상태별 카운트
        Map<String, Long> statusCounts = new java.util.LinkedHashMap<>();
        for (com.goodee.beedan.common.constant.QuoteStatus s : com.goodee.beedan.common.constant.QuoteStatus.values()) {
            statusCounts.put(s.name(), quoteList.stream().filter(q -> q.getQuStt() == s).count());
        }

        // 견적 상세 데이터 조합
        List<Map<String, Object>> quotes = new ArrayList<>();
        for (QuoteBase qb : quoteList) {
            Map<String, Object> item = new java.util.LinkedHashMap<>();
            item.put("quId", qb.getQuId());
            item.put("quCd", qb.getQuCd());
            item.put("quStt", qb.getQuStt().name());
            item.put("quOpYn", qb.getQuUsOpYn() != null && qb.getQuUsOpYn());
            item.put("quCreDt", qb.getQuCreDt());
            item.put("quUpdDt", qb.getQuUpdDt());

            quotes.add(item);
        }

        model.addAttribute("negotiation", negotiation);
        model.addAttribute("quotes", quotes);
        model.addAttribute("statusCounts", statusCounts);

        return "/quote/negotiation-detail";
    }

}
