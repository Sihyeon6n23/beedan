package com.example.beedan.repository.cart;

import com.example.beedan.entity.Cart;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Cart, Long> {
    // 현재 세션에 접속중인 member의 id로 장바구니 목록 조회
    Page<Cart> findByMemId(Long memId, Pageable pageable);
}
