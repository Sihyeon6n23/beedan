package com.goodee.beedan.repository.quote;

import com.goodee.beedan.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    Optional<OrderItem> findByOrdItmThumbKey(String ordItmThumbKey);
}
