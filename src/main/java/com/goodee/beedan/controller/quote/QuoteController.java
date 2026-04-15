package com.goodee.beedan.controller.quote;

import com.goodee.beedan.common.constant.MemberAuthority;
import com.goodee.beedan.common.constant.QuoteStatus;
import com.goodee.beedan.config.security.MemberUserDetails;
import com.goodee.beedan.dto.quote.CartToQuoteDto;
import com.goodee.beedan.dto.quote.NegotiationRequest;
import com.goodee.beedan.dto.quote.QuoteBaseRequest;
import com.goodee.beedan.dto.quote.QuoteRequestDto;
import com.goodee.beedan.entity.*;
import com.goodee.beedan.repository.buyer.BuyerGradePolicyRepository;
import com.goodee.beedan.repository.chat.ChatRoomRepository;
import com.goodee.beedan.repository.member.MemberRepository;
import com.goodee.beedan.repository.quote.*;
import com.goodee.beedan.repository.receiver.ReceiverRepository;
import com.goodee.beedan.repository.stock.StockRepository;
import com.goodee.beedan.service.exchangeRate.ExchangeRateService;
import com.goodee.beedan.service.quote.*;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Controller
@RequestMapping("/quote")
@RequiredArgsConstructor
public class    QuoteController {

    private final NegotiationService negotiationService;
    private final QuoteBaseService quoteBaseService;
    private final QuoteBaseRepository quoteBaseRepository;
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
    private final QuoteNameService quoteNameService;
    private final FactoryRepository factoryRepository;
    private final com.goodee.beedan.repository.pageview.PageViewRepository pageViewRepository;
    private final ShippingRateRepository shippingRateRepository;
    private final NegotiationRepository negotiationRepository;
    private final ChatRoomRepository chatRoomRepository;

    @PostMapping("/request")
    @ResponseBody
    public ResponseEntity<Map<String, String>> postItems(
            @RequestBody @Valid QuoteRequestDto dto,
            @AuthenticationPrincipal MemberUserDetails userDetails,
            HttpSession session) {

        Long memId = userDetails.getMemberId();

        if (dto.getChatRoomId() != null && isAdminUser(memId)) {
            return ResponseEntity.ok(createAdminChatQuoteRedirect(dto, memId, session));
        }

        // AI로 협상 이름 생성
        Member member = memberRepository.findById(memId).orElse(null);
        String companyName = (member != null && member.getMemBizTtl() != null)
                ? member.getMemBizTtl() : "고객";
        List<String> productNames = new ArrayList<>();
        for (var item : dto.getItems()) {
            Stock stock = stockRepository.findById(item.getStId()).orElse(null);
            if (stock != null) productNames.add(stock.getStNm());
        }
        String ngNm;
        try {
            ngNm = quoteNameService.generateName(companyName, productNames);
        } catch (Exception e) {
            ngNm = companyName + "_견적";
        }

        // 1. Negotiation 생성
        Negotiation negotiation = negotiationService.create(
                NegotiationRequest.builder()
                        .ngNm(ngNm)
                        .memId(memId)
                        .build()
        );

        // 2. QuoteBase 생성 (QU 코드 = 협상 내 순번)
        QuoteBase quoteBase = quoteBaseService.create(
                QuoteBaseRequest.builder()
                        .ngId(negotiation.getNgId())
                        .quSid(memId)
                        .quRid(memId)
                        .build()
        );

        // 3. 세션에 견적 항목 저장 (getWrite에서 사용)
        session.setAttribute("quoteItems_" + quoteBase.getQuId(), dto.getItems());

        String redirectUrl = "/quote/write?quId=" + quoteBase.getQuId();
        return ResponseEntity.ok(Map.of("redirectUrl", redirectUrl));
    }

    private boolean isAdminUser(Long memId) {
        return memberRepository.findById(memId)
                .map(member -> MemberAuthority.ADMIN.equals(member.getMemAut()))
                .orElse(false);
    }

    private Map<String, String> createAdminChatQuoteRedirect(QuoteRequestDto dto, Long memAdId, HttpSession session) {
        // 채팅방 확인
        ChatRoom chatRoom = chatRoomRepository.findById(dto.getChatRoomId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채팅방입니다."));

        // 현재 로그인한 관리자가 이 채팅방 담당자인지 확인
        if (chatRoom.getMemAdId() == null || !chatRoom.getMemAdId().equals(memAdId)) {
            throw new IllegalStateException("담당 관리자만 채팅 견적 초안을 작성할 수 있습니다.");
        }

        Long memberId = chatRoom.getMemId();

        Member member = memberRepository.findById(memberId).orElse(null);

        // 협상명 생성용 회사명
        String companyName = (member != null && member.getMemBizTtl() != null && !member.getMemBizTtl().isBlank())
                ? member.getMemBizTtl()
                : "고객";

        List<String> productNames = new ArrayList<>();
        for (QuoteRequestDto.QuoteRequestItemDto item : dto.getItems()) {
            Stock stock = stockRepository.findById(item.getStId()).orElse(null);
            if (stock != null && stock.getStNm() != null) {
                productNames.add(stock.getStNm());
            }
        }

        String ngNm;
        try {
            ngNm = quoteNameService.generateName(companyName, productNames);
        } catch (Exception e) {
            ngNm = companyName + "_견적";
        }

        // 협상은 사용자 소유
        Negotiation negotiation = negotiationService.create(
                NegotiationRequest.builder()
                        .ngNm(ngNm)
                        .memId(memberId)
                        .build()
        );

        // 초안은 관리자 -> 사용자 방향의 견적
        QuoteBase quoteBase = quoteBaseService.create(
                QuoteBaseRequest.builder()
                        .ngId(negotiation.getNgId())
                        .quSid(memberId)
                        .quRid(memAdId)
                        .build()
        );

        // 장바구니 선택 상품은 세션으로 write 페이지에 전달
        session.setAttribute("quoteItems_" + quoteBase.getQuId(), dto.getItems());

        return Map.of(
                "redirectUrl",
                "/admin/quote/write?quId=" + quoteBase.getQuId() + "&chatRoomId=" + dto.getChatRoomId()
        );
    }

@GetMapping("/list")
    public String getList(@AuthenticationPrincipal MemberUserDetails userDetails,
                          @RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "desc") String sort,
                          Model model) {
        Long memId = userDetails.getMemberId();

        int pageSize = 10;
        org.springframework.data.domain.Sort sortOrder = "asc".equals(sort)
                ? org.springframework.data.domain.Sort.by("quCreDt").ascending()
                : org.springframework.data.domain.Sort.by("quCreDt").descending();
        model.addAttribute("sort", sort);

        // 견적 + 협상명 조인 조회 (1 쿼리)
        Page<Object[]> quPage = quoteBaseRepository.findAllByMemberWithNgNm(memId, PageRequest.of(page - 1, pageSize, sortOrder));

        List<Map<String, Object>> quotes = new ArrayList<>();
        for (Object[] row : quPage.getContent()) {
            Long quId = (Long) row[0];
            QuoteStatus quStt = (QuoteStatus) row[1];
            Boolean quUsOpYn = (Boolean) row[2];
            Boolean quAdOpYn = (Boolean) row[3];
            String quCd = (String) row[4];
            java.time.LocalDateTime quCreDt = (java.time.LocalDateTime) row[5];
            java.time.LocalDateTime quUpdDt = (java.time.LocalDateTime) row[6];
            Long quSid = (Long) row[7];
            Long quRid = (Long) row[8];
            String ngNm = (String) row[9];

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("quId", quId);
            item.put("quStt", quStt.name());
            item.put("quOpYn", quUsOpYn != null && quUsOpYn);
            item.put("quAdOpYn", quAdOpYn != null && quAdOpYn);
            item.put("quCd", quCd);
            item.put("ngNm", ngNm != null ? ngNm : "");
            item.put("quCreDt", quCreDt);
            item.put("quUpdDt", quUpdDt);

            String displayStt = quStt.name();
            if (quStt == QuoteStatus.SUBMITTED) {
                boolean sender = memId.equals(quSid) || (quSid == null && memId.equals(quRid));
                if (!sender) {
                    displayStt = (quUsOpYn != null && quUsOpYn) ? "CONFIRMED" : "UNREAD";
                }
            }
            item.put("displayStt", displayStt);
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
                           @RequestParam(required = false) Long chatRoomId,
                           Model model,
                           HttpSession session,
                           @AuthenticationPrincipal MemberUserDetails userDetails) {

        // 재작성 진입: fromQuId → 기존 견적에서 새 견적 생성
        Long sourceQuId = quId; // 데이터를 로드할 원본 quId
        if (fromQuId != null) {
            QuoteBase oldQuote = quoteBaseService.findById(fromQuId);
            if (oldQuote == null) {
                throw new NoSuchElementException("재작성할 견적을 찾을 수 없습니다.");
            }
            Negotiation oldNegotiation = negotiationService.findById(oldQuote.getNgId());
            if (oldNegotiation == null
                    || !oldNegotiation.getMemId().equals(userDetails.getMemberId())) {
                throw new AccessDeniedException("해당 견적에 접근할 권한이 없습니다.");
            }

            // 송신자 = 나, 수신자 = 상대방
            Long myId = userDetails != null ? userDetails.getMemberId() : null;
            Long receiverId = oldQuote.getQuSid() != null ? oldQuote.getQuSid() : oldQuote.getQuRid();
            if (receiverId != null && receiverId.equals(myId)) {
                receiverId = oldQuote.getQuRid() != null ? oldQuote.getQuRid() : oldQuote.getQuSid();
            }
            QuoteBase newQuote = quoteBaseService.create(
                    new com.goodee.beedan.dto.quote.QuoteBaseRequest(
                            oldQuote.getNgId(), myId, receiverId));

            newQuote.tempSave(); // 재작성은 바로 TEMP_SAVE (데이터가 프리필되므로)
            quId = newQuote.getQuId();
            sourceQuId = fromQuId;
            model.addAttribute("rejectedReason", oldQuote.getQuCon());
            model.addAttribute("rewriteFromQuId", fromQuId);
            model.addAttribute("rewriteFromQuCd", oldQuote.getQuCd());

        }

        if (quId == null) {
            throw new IllegalArgumentException("견적 정보가 없습니다.");
        }

        QuoteBase quoteBase = quoteBaseService.findById(quId);
        if (quoteBase == null) {
            throw new NoSuchElementException("해당 견적을 찾을 수 없습니다.");
        }

        Long myId = userDetails != null ? userDetails.getMemberId() : null;
        Negotiation negotiation = negotiationService.findById(quoteBase.getNgId());
        if (negotiation == null || !negotiation.getMemId().equals(myId)) {
            throw new AccessDeniedException("해당 견적에 접근할 권한이 없습니다.");
        }

        // 사용자 소유 협상의 TEMP_SAVE 초안은, 사용자가 송신자인 경우 이어서 작성 가능
        boolean isUserOwnedTempDraft = quoteBase.getQuStt() == QuoteStatus.TEMP_SAVE
                && negotiation != null
                && myId != null
                && myId.equals(negotiation.getMemId())
                && myId.equals(quoteBase.getQuSid());

        // 재작성이 아닌 일반 진입일 때만 editable 체크
        // 일반 수정 가능 여부 + 사용자 수신 TEMP_SAVE 초안 허용
        if (fromQuId == null && !quoteBase.isEditable() && !isUserOwnedTempDraft) {
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

        model.addAttribute("chatRoomId", chatRoomId);

        // 견적 코드 + 협상명
        model.addAttribute("quCd", quoteBase.getQuCd());
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
            } catch (Exception e) { /* non-critical */ }
        }

        model.addAttribute("countryCodes", shippingRateRepository.findDistinctCountryCodes());


        return "/quote/quote-write";
    }

    @GetMapping("/detail")
    public String getDetail(@RequestParam Long quId,
                            @AuthenticationPrincipal MemberUserDetails userDetails,
                            Model model) {
        QuoteBase quoteBase = quoteBaseService.findById(quId);
        if (quoteBase == null) {
            throw new NoSuchElementException("해당 견적을 찾을 수 없습니다.");
        }

        Long myId = userDetails.getMemberId();
        Negotiation negotiation = negotiationService.findById(quoteBase.getNgId());
        if (negotiation == null || !negotiation.getMemId().equals(myId)) {
            throw new AccessDeniedException("해당 견적에 접근할 권한이 없습니다.");
        }

        // 상대방의 TEMP_SAVE 견적 접근 차단
        if (QuoteStatus.TEMP_SAVE.equals(quoteBase.getQuStt())
                && !myId.equals(quoteBase.getQuSid())) {
            throw new AccessDeniedException("임시저장된 견적은 작성자만 조회할 수 있습니다.");
        }

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
            // Factory name lookup
            String faNm = null;
            if (d.getFaId() != null) {
                try { faNm = factoryRepository.findById(d.getFaId()).map(Factory::getFaNm).orElse(null); } catch (Exception e) { /* non-critical */ }
            }
            extra.put("faNm", faNm);
            // UnitGroup name lookup
            String unGNm = null;
            if (d.getUnGId() != null) {
                try { unGNm = unitGroupService.findById(d.getUnGId()).getUnGNm(); } catch (Exception e) { /* non-critical */ }
            }
            extra.put("unGNm", unGNm);
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
        boolean isSender = myId.equals(quoteBase.getQuSid())
                || (quoteBase.getQuSid() == null && myId.equals(quoteBase.getQuRid()));
        model.addAttribute("isSender", isSender);
        model.addAttribute("isAdmin", false);


        return "admin/quote/admin-quote-detail";
    }
    @GetMapping("/negotiation/list")
    public String getNegotiationList(@AuthenticationPrincipal MemberUserDetails userDetails,
                                     @RequestParam(defaultValue = "1") int page,
                                     Model model) {
        Long memId = userDetails.getMemberId();

        // 유효 견적이 있는 협상 + 견적 수 + 미열람 수 (1 쿼리)
        List<Map<String, Object>> allFiltered = new ArrayList<>();
        for (Object[] row : negotiationRepository.findNegotiationsWithQuoteSummary(memId)) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("ngId", (Long) row[0]);
            item.put("ngNm", (String) row[1]);
            item.put("ngCreDt", row[2]);
            item.put("ngEndDt", row[3]);
            item.put("ongoing", row[3] == null);
            item.put("quoteCount", ((Number) row[4]).intValue());
            item.put("hasUnread", ((Number) row[5]).longValue() > 0);
            allFiltered.add(item);
        }

        // 수동 페이징
        int pageSize = 10;
        int totalItems = allFiltered.size();
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);
        int fromIndex = Math.min((page - 1) * pageSize, totalItems);
        int toIndex = Math.min(fromIndex + pageSize, totalItems);
        List<Map<String, Object>> negotiations = allFiltered.subList(fromIndex, toIndex);

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
        Negotiation negotiation = negotiationService.findById(ngId);
        if (negotiation == null) {
            throw new NoSuchElementException("협상을 찾을 수 없습니다.");
        }

        // 소유자 검증
        if (!negotiation.getMemId().equals(userDetails.getMemberId())) {
            throw new AccessDeniedException("해당 협상에 접근할 권한이 없습니다.");
        }

        Long memId = userDetails.getMemberId();

        // 해당 협상의 유효 견적 목록 (상대방 TEMP_SAVE 제외)
        List<QuoteBase> quoteList = quoteBaseRepository.findAllActiveByNgId(ngId, memId);

        // 읽지 않음 카운트 (열람 처리 전에 계산)
        long unreadCount = quoteList.stream()
                .filter(qb -> qb.getQuStt() == com.goodee.beedan.common.constant.QuoteStatus.SUBMITTED)
                .filter(qb -> {
                    boolean sender = memId.equals(qb.getQuSid()) || (qb.getQuSid() == null && memId.equals(qb.getQuRid()));
                    return !sender && (qb.getQuUsOpYn() == null || !qb.getQuUsOpYn());
                })
                .count();

        // 수신자 본인의 미열람 견적 열람 처리
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

            // 표시용 상태
            String displayStt = qb.getQuStt().name();
            if (qb.getQuStt() == com.goodee.beedan.common.constant.QuoteStatus.SUBMITTED) {
                boolean sender = memId.equals(qb.getQuSid()) || (qb.getQuSid() == null && memId.equals(qb.getQuRid()));
                if (!sender) {
                    displayStt = (qb.getQuUsOpYn() != null && qb.getQuUsOpYn()) ? "CONFIRMED" : "UNREAD";
                }
            }
            item.put("displayStt", displayStt);

            quotes.add(item);
        }

        model.addAttribute("negotiation", negotiation);
        model.addAttribute("quotes", quotes);
        model.addAttribute("statusCounts", statusCounts);
        model.addAttribute("unreadCount", unreadCount);

        return "/quote/negotiation-detail";
    }

}
