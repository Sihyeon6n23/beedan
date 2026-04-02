package com.goodee.beedan.controller.admin;

import com.goodee.beedan.entity.*;
import com.goodee.beedan.repository.member.MemberRepository;
import com.goodee.beedan.repository.quote.QuoteInfoRepository;
import com.goodee.beedan.service.quote.NegotiationService;
import com.goodee.beedan.service.quote.QuoteBaseService;
import com.goodee.beedan.service.quote.QuoteDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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

            // 견적 수
            List<QuoteBase> quotes = quoteBaseService.findAllByNego(ng.getNgId());
            item.put("quoteCount", quotes.size());

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
    public String negotiationDetail() {
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
            item.put("quStt", qb.getQuStt().name());
            item.put("quCreDt", qb.getQuCreDt());

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
    public String quoteDetail() {
        return "admin/quote/admin-quote-detail";
    }

    @GetMapping("/quote/write")
    public String quoteWrite() {
        return "admin/quote/admin-quote-write";
    }
}
