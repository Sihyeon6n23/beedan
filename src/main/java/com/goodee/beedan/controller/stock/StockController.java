package com.goodee.beedan.controller.stock;

import com.goodee.beedan.config.security.MemberUserDetails;
import com.goodee.beedan.dto.buyer.BuyerGradePolicyResponse;
import com.goodee.beedan.entity.BuyerGradePolicy;
import com.goodee.beedan.entity.Stock;
import com.goodee.beedan.service.buyer.BuyerGradePolicyService;
import com.goodee.beedan.service.stock.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class StockController {
    private final StockService stockService;
    private final BuyerGradePolicyService buyerGradePolicyService;

    // 상품 목록
    @GetMapping("/stock/list")
    public String getStocks(Model model,
                            @AuthenticationPrincipal MemberUserDetails userDetails) {
        Long memId = userDetails != null ? userDetails.getMemberId() : null;
        model.addAttribute("memberId", memId);
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
    public String getStockDetail(@PathVariable Long stId, Model model,
                                 @AuthenticationPrincipal MemberUserDetails userDetails) {
        Long memId = userDetails != null ? userDetails.getMemberId() : null;
        Stock stock = stockService.findById(stId);
        boolean wished = stockService.isWished(stId, memId);

        List<BuyerGradePolicy> policies = buyerGradePolicyService.findAllActiveOrdered();

        model.addAttribute("stock", stock);
        model.addAttribute("wished", wished);
        model.addAttribute("memberId", memId);
        model.addAttribute("policyList", policies.stream().map(BuyerGradePolicyResponse::from).toList());
        return "stock/stock-detail";
    }
}
