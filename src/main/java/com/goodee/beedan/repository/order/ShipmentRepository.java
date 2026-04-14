package com.goodee.beedan.repository.order;

import com.goodee.beedan.common.constant.ShipmentStatus;
import com.goodee.beedan.config.exception.EntityNotFoundException;
import com.goodee.beedan.entity.Order;
import com.goodee.beedan.entity.Shipment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
    Page<Shipment> findByOrder_OrdBaseIdOrderByShCreDtDesc(Long ordBaseId, Pageable pageable);

    List<Shipment> findTop3ByOrder_Member_MemIdOrderByShCreDtDesc(Long memId);

    @Query("SELECT s FROM Shipment s " +
            "JOIN s.order o " +
            "WHERE o.member.memId = :memId " +
            "AND s.shCanYn = false")
    Page<Shipment> findByMemberId(@Param("memId") Long memId, Pageable pageable);

    List<Shipment> findByShSttIn(List<ShipmentStatus> statuses);

    List<Shipment> findByOrder(Order order);

    default Shipment getByIdOrThrow(Long shId) {
        return findById(shId).orElseThrow(() -> new EntityNotFoundException("배송 내역을 찾을 수 없습니다. ID: " + shId));
    }
}
