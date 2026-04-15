package com.goodee.beedan.controller.stock;

import com.goodee.beedan.config.security.MemberUserDetails;
import com.goodee.beedan.dto.buyer.BuyerGradePolicyResponse;
import com.goodee.beedan.entity.BuyerGradePolicy;
import com.goodee.beedan.entity.Stock;
import com.goodee.beedan.service.buyer.BuyerGradePolicyService;
import com.goodee.beedan.service.stock.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class StockController {
    private final StockService stockService;
    private final BuyerGradePolicyService buyerGradePolicyService;
    private final com.goodee.beedan.repository.pageview.PageViewRepository pageViewRepository;

    // 상품 목록
    @GetMapping("/stock/list")
    public String getStocks(Model model,
                            @RequestParam(required = false) Long chatRoomId,
                            @AuthenticationPrincipal MemberUserDetails userDetails) {
        Long memId = userDetails != null ? userDetails.getMemberId() : null;
        model.addAttribute("memberId", memId);
        model.addAttribute("chatRoomId", chatRoomId);
        model.addAttribute("brands", stockService.findAllBrands());
        model.addAttribute("categories", stockService.findAllCategories());

        return "stock/stock-list";
    }

    // 내 상품 목록
    @GetMapping("/myitem")
    public String getMyItems(Model model,
                             @AuthenticationPrincipal MemberUserDetails userDetails) {
        Long memId = userDetails != null ? userDetails.getMemberId() : null;
        model.addAttribute("memberId", memId);
        model.addAttribute("brands", stockService.findAllBrands());
        model.addAttribute("categories", stockService.findAllCategories());
        return "stock/myitem";
    }

    // 상품 상세
    @GetMapping("/stock/detail/{stId}")
    public String getStockDetail(@PathVariable Long stId,
                                 @RequestParam(required = false) Long chatRoomId,
                                 Model model,
                                 @AuthenticationPrincipal MemberUserDetails userDetails) {
        Long memId = userDetails != null ? userDetails.getMemberId() : null;
        Stock stock = stockService.findById(stId);
        boolean wished = stockService.isWished(stId, memId);

        // 요청 상품인지
        boolean isReqItem = stock.isStReqYn();

        if (isReqItem) {
            if (userDetails == null) {
                throw new AccessDeniedException("로그인이 필요합니다.");
            }
            boolean isUser = userDetails.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_USER"));
            if (isUser && !stock.getStReqMemId().equals(memId)) {
                throw new AccessDeniedException("요청 상품은 본인만 조회할 수 있습니다.");
            }
        }

        List<BuyerGradePolicy> policies = buyerGradePolicyService.findAllActiveOrdered();

        model.addAttribute("stock", stock);
        model.addAttribute("wished", wished);
        model.addAttribute("memberId", memId);
        model.addAttribute("chatRoomId", chatRoomId);
        model.addAttribute("policyList", policies.stream().map(BuyerGradePolicyResponse::from).toList());
        return "stock/stock-detail";
    }
}
