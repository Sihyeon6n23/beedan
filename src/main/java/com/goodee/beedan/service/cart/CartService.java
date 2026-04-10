package com.goodee.beedan.service.cart;

import com.goodee.beedan.dto.cart.CartDto;
import com.goodee.beedan.dto.cart.CartUpdateDto;
import com.goodee.beedan.entity.Cart;
import com.goodee.beedan.entity.ExchangeRate;
import com.goodee.beedan.entity.Stock;
import com.goodee.beedan.repository.cart.CartRepository;
import com.goodee.beedan.repository.stock.StockRepository;
import com.goodee.beedan.service.exchangeRate.ExchangeRateService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {
    private final CartRepository cartRepository;
    private final ExchangeRateService exchangeRateService;
    private final StockRepository stockRepository;

    // 장바구니 목록 (페이징 포함)
    public List<CartDto> findByMemId(Long memId) {
        return cartRepository.findByMemId(memId)
                .stream()
                .map(this::mapToCartDto)
                .toList();
    }


    // 장바구니에 추가
    public boolean addItem(Long memId, Long stId, Long qn) {
        Optional<Cart> itemExists = cartRepository.findByMemIdAndStock_StId(memId, stId);
        if (itemExists.isPresent()) {
            return true;
        }
        Stock stock = stockRepository.findById(stId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다."));
        if(!stock.isStExpYn()) {
            throw new IllegalArgumentException("주문할 수 없는 상품입니다.");
        }
        Cart cart = Cart.builder()
                .memId(memId)
                .caStQn(qn)
                .stock(stock)
                .build();
        cartRepository.save(cart);
        return false;
    }

    // 장바구니에서 상품 삭제
    public void deleteItem(Long caId, Long memId) {
        Cart cart = cartRepository.findById(caId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 장바구니 항목입니다."));
        if (!cart.getMemId().equals(memId)) {
            throw new AccessDeniedException("장바구니에 대한 권한이 없습니다.");
        }
        cartRepository.deleteById(caId);
    }

    // 회원의 장바구니 전체 비우기
    public void clearCart(Long memId) {
        cartRepository.deleteAllByMemId(memId);
    }

    // 회원의 장바구니에서 특정 상품만 삭제 (별도 트랜잭션)
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void clearCartItems(Long memId, java.util.List<Long> stIds) {
        if (stIds != null && !stIds.isEmpty()) {
            cartRepository.deleteAllByMemIdAndStock_StIdIn(memId, stIds);
        }
    }

    // 장바구니 상품 수량 수정
    public void updateCart(List<CartUpdateDto> updates, Long memId) {
        for (CartUpdateDto dto : updates) {
            // DB에 해당 caId(장바구니 상세)가 있는지 확인
            Cart cart = cartRepository.findById(dto.getCaId()).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 장바구니 항목입니다."));
            if (!cart.getMemId().equals(memId)) {
                throw new AccessDeniedException("장바구니에 대한 권한이 없습니다.");
            }
            cart.setCaStQn(dto.getCaStQn());
        }
    }
    // 상품 가격 -> 원화 환산
    public BigDecimal getRate(String symbol) {
        if(symbol.equals("₩")) {
            return BigDecimal.valueOf(1);
        } else {
            ExchangeRate rate = exchangeRateService.findLatestByCurrencySymbol(symbol);
            if (rate == null) {
                throw new IllegalArgumentException("환율 정보를 가져올 수 없습니다: " + symbol);
            }
            return rate.getErRa();
        }
    }
    // 장바구니 엔티티 객체를 DTO 객체로 변환
    public CartDto mapToCartDto(Cart cart) {
        BigDecimal erRa = getRate(cart.getStock().getStCur());
        BigDecimal toKrw = cart.getStock().getStPr().multiply(erRa).setScale(-2, RoundingMode.HALF_UP);
        return CartDto.builder()
                .caId(cart.getCaId())
                .caStQn(cart.getCaStQn())
                .memId(cart.getMemId())
                .stId(cart.getStock().getStId())
                .stCd(cart.getStock().getStCd())
                .stBrNm(cart.getStock().getStBrNm())
                .stNm(cart.getStock().getStNm())
                .stPr(cart.getStock().getStPr())
                .stKrwPr(toKrw)
                .stCur(cart.getStock().getStCur())
                .stImgUrl(cart.getStock().getStImgUrl())
                .stUseYn(cart.getStock().isStUseYn())
                .build();
    }
}
