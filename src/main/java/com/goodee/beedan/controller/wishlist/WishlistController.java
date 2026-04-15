package com.goodee.beedan.controller.wishlist;

import com.goodee.beedan.config.security.MemberUserDetails;
import com.goodee.beedan.service.stock.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/wishlist")
public class WishlistController {

    private final StockService stockService;

    @GetMapping
    public String getWishlistItems(Model model,
            @AuthenticationPrincipal MemberUserDetails userDetails) {
        Long memId = userDetails.getMemberId();
        model.addAttribute("memberId", memId);
        model.addAttribute("brands", stockService.findAllBrands());
        model.addAttribute("categories", stockService.findAllCategories());
        model.addAttribute("mode", "wishlist");
        return "stock/stock-list";
    }
}
