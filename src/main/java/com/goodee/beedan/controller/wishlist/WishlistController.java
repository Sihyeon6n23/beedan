package com.goodee.beedan.controller.wishlist;

import com.goodee.beedan.config.security.MemberUserDetails;
import com.goodee.beedan.dto.stock.StockListDto;
import com.goodee.beedan.entity.Brand;
import com.goodee.beedan.entity.Category;
import com.goodee.beedan.entity.Wishlist;
import com.goodee.beedan.service.stock.StockService;
import com.goodee.beedan.service.wishlist.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;
    private final StockService stockService;

    @GetMapping
    public String getWishlistItems(
            @PageableDefault(page = 0, size = 8, sort = "stId", direction = Sort.Direction.DESC)
            Pageable pageable,
            Model model,
            @AuthenticationPrincipal MemberUserDetails userDetails) {
        Long memId = userDetails.getMemberId();
        model.addAttribute("memberId", memId);

        // 해당 회원의 wishlist에 저장되어 있는 상품 목록
        List<Wishlist> wishedItems = wishlistService.findAllWishedItems(memId);
        Page<StockListDto> page = stockService.findWishedItems(pageable, wishedItems);
        List<Brand> brands = stockService.findAllBrands();
        List<Category> categories = stockService.findAllCategories();

        model.addAttribute("page", page);
        model.addAttribute("brands", brands);
        model.addAttribute("categories", categories);
        model.addAttribute("mode", "wishlist");
        return "stock/stock-list";
    }
}
