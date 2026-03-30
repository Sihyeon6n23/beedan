package com.goodee.beedan.repository.order;

import com.goodee.beedan.entity.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
    List<Shipment> findByOrder_OrdBaseIdOrderByShCreDtDesc(Long ordBaseId);
}
