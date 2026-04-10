package com.goodee.beedan.repository.order;

import com.goodee.beedan.common.constant.ShipmentStatus;
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

    List<Shipment> findByShStt(ShipmentStatus status);
}
