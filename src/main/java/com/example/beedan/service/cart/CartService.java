package com.example.beedan.service.cart;

import com.example.beedan.dto.cart.CartDto;
import com.example.beedan.entity.Cart;
import com.example.beedan.repository.cart.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {
    private final CartRepository cartRepository;

    // 장바구니 목록 (페이징 포함)
    public Page<CartDto> findByMemId(Long memId, Pageable pageable) {
        return cartRepository.findByMemId(memId, pageable)
                .map(this::mapToCartDto);
    }

    // 장바구니 엔티티 객체를 DTO 객체로 변환
    public CartDto mapToCartDto(Cart cart) {
        return CartDto.builder()
                .caId(cart.getCaId())
                .caStQn(cart.getCaStQn())
                .memId(cart.getMemId())
                .stId(cart.getStock().getStId())
                .stCd(cart.getStock().getStCd())
                .brNm(cart.getStock().getBrNm())
                .stNm(cart.getStock().getStNm())
                .stPr(cart.getStock().getStPr())
                .stCur(cart.getStock().getStCur())
                .stImgUrl(cart.getStock().getStImgUrl())
                .stUseYn(cart.getStock().isStUseYn())
                .build();
    }
}
