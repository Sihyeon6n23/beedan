package com.goodee.beedan.controller.cart;

import com.goodee.beedan.config.security.MemberUserDetails;
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
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/cart")
public class CartController {
    private final CartService cartService;

    // 장바구니 목록 (페이지 네이션 적용)
    @GetMapping
    public String getCartItems(
            @RequestParam(required = false) Long chatRoomId,
            @AuthenticationPrincipal MemberUserDetails userDetails,
            Model model) {
        Long memId = userDetails != null ? userDetails.getMemberId() : null;
        List<CartDto> list = cartService.findByMemId(memId);
        model.addAttribute("itemList", list);
        model.addAttribute("chatRoomId", chatRoomId);
        return "cart/cart";
    }
}
