package com.goodee.beedan.repository.order;

import com.goodee.beedan.config.exception.EntityNotFoundException;
import com.goodee.beedan.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByMember_MemIdOrderByOrdBaseCreDtDesc(Long memId, Pageable pageable);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.shipments WHERE o.ordBaseId = :ordId")
    Optional<Order> findByIdWithShipments(@Param("ordId") Long ordId);

    List<Order> findTop3ByMember_MemIdOrderByOrdBaseCreDtDesc(Long memId);

    default Order getByIdOrThrow(Long ordId) {
        return findByIdWithShipments(ordId).orElseThrow(() -> new EntityNotFoundException("주문을 찾을 수 없습니다. ID: " + ordId));
    }


}