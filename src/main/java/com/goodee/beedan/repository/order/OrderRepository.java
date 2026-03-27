package com.goodee.beedan.repository.order;

import com.goodee.beedan.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByMember_MemIdOrderByOrdBaseCreDtDesc(Long memId);
}
