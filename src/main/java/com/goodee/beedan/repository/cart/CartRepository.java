package com.goodee.beedan.repository.cart;

import com.goodee.beedan.entity.Cart;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    // 현재 세션에 접속중인 member의 id로 장바구니 목록 조회
    List<Cart> findByMemId(Long memId);

    Optional<Cart> findByMemIdAndStock_StId(Long memId, Long stId);

    void deleteAllByMemId(Long memId);

    void deleteAllByMemIdAndStock_StIdIn(Long memId, List<Long> stIds);
}
