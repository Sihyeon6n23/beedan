package com.goodee.beedan.service.cart;

import com.goodee.beedan.dto.cart.CartDto;
import com.goodee.beedan.dto.cart.CartUpdateDto;
import com.goodee.beedan.entity.Cart;
import com.goodee.beedan.repository.cart.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    // 장바구니에서 상품 삭제
    public void deleteItem(Long caId) {
        cartRepository.deleteById(caId);
    }

    // 장바구니 상품 수량 수정
    public void updateCart(List<CartUpdateDto> updates) {
        for (CartUpdateDto dto : updates) {
            // DB에 해당 caId(장바구니 상세)가 있는지 확인
            Cart cart = cartRepository.findById(dto.getCaId()).orElse(null);
            if (cart != null) {
                cart.setCaStQn(dto.getCaStQn());
                cartRepository.save(cart);
            }
        }
    }

    // 장바구니 엔티티 객체를 DTO 객체로 변환
    public CartDto mapToCartDto(Cart cart) {
        return CartDto.builder()
                .caId(cart.getCaId())
                .caStQn(cart.getCaStQn())
                .memId(cart.getMemId())
                .stId(cart.getStock().getStId())
                .stCd(cart.getStock().getStCd())
                .stBrNm(cart.getStock().getStBrNm())
                .stNm(cart.getStock().getStNm())
                .stPr(cart.getStock().getStPr())
                .stCur(cart.getStock().getStCur())
                .stImgUrl(cart.getStock().getStImgUrl())
                .stUseYn(cart.getStock().isStUseYn())
                .build();
    }
}
