package com.goodee.beedan.repository.order;

import com.goodee.beedan.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
