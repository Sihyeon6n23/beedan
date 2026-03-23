package com.goodee.beedan.controller.cart;

import com.goodee.beedan.dto.cart.CartDto;
import com.goodee.beedan.service.cart.CartService;
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

@Controller
@RequiredArgsConstructor
@RequestMapping("/cart")
public class CartController {
    private final CartService cartService;


    // 장바구니 목록 (페이지 네이션 적용)
    @GetMapping
    public String getCartItems(
            @PageableDefault(page = 0, size = 5, sort = "caId", direction = Sort.Direction.DESC)
            Pageable pageable,
//            @AuthenticationPrincipal MemberUserDetails userDetails,
            Model model) {
//        Long memId = userDetails.getMemberId();
        Long memId = 1L;
        Page<CartDto> page = cartService.findByMemId(memId, pageable);
        model.addAttribute("page", page);
        return "cart/cart";
    }
}
