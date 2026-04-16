package com.goodee.beedan.controller.cart;

import com.goodee.beedan.config.security.MemberUserDetails;
import com.goodee.beedan.dto.cart.CartUpdateDto;
import com.goodee.beedan.entity.PageView;
import com.goodee.beedan.repository.cart.CartRepository;
import com.goodee.beedan.repository.pageview.PageViewRepository;
import com.goodee.beedan.service.cart.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartApiController {

    private static final String PAGE_ADD_TO_CART = "ADD_TO_CART";

    private final CartService cartService;
    private final PageViewRepository pageViewRepository;

    @DeleteMapping("/{caId}")
    public ResponseEntity<Void> delete(@PathVariable Long caId,
                                        @AuthenticationPrincipal MemberUserDetails userDetails) {
        cartService.deleteItem(caId, userDetails.getMemberId());
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/update")
    public ResponseEntity<Void> updateCart(@RequestBody List<CartUpdateDto> updates,
                                           @AuthenticationPrincipal MemberUserDetails userDetails) {
        cartService.updateCart(updates, userDetails.getMemberId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{stId}")
    public ResponseEntity<Boolean> addToCart(
                                        @PathVariable Long stId,
                                        @RequestParam Long qn,
                                        @AuthenticationPrincipal MemberUserDetails userDetails) {
        boolean exists = cartService.addItem(userDetails.getMemberId(), stId, qn);
        // 대시보드 퍼널/인기상품 집계용 — 장바구니 담기 이벤트 기록
        pageViewRepository.save(PageView.builder()
                .page(PAGE_ADD_TO_CART)
                .refId(stId)
                .memId(userDetails.getMemberId())
                .build());
        return ResponseEntity.ok(exists);
    }
}
