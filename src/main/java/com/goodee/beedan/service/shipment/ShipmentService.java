package com.goodee.beedan.service.shipment;

import com.goodee.beedan.common.constant.OrderStatus;
import com.goodee.beedan.common.constant.ShipmentStatus;
import com.goodee.beedan.dto.order.ShipmentDto;
import com.goodee.beedan.entity.Order;
import com.goodee.beedan.entity.Shipment;
import com.goodee.beedan.repository.order.OrderRepository;
import com.goodee.beedan.repository.order.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ShipmentService {
    private final ShipmentRepository shipmentRepository;
    private final OrderRepository orderRepository;

    @Transactional(readOnly = true)
    public Page<ShipmentDto> getShipmentList(Long memId, Long ordId, Pageable pageable) {
        Order order = orderRepository.getByIdOrThrow(ordId);

        if (!order.getMember().getMemId().equals(memId)) throw new IllegalArgumentException("본인의 주문 배송 목록만 조회할 수 있습니다.");

        Page<Shipment> shipmentPage = shipmentRepository.findByOrder_OrdBaseIdOrderByShCreDtDesc(ordId, pageable);
        return shipmentPage.map(this::mapToShipmentDto);
    }

    @Transactional(readOnly = true)
    public ShipmentDto getShipmentDetail(Long shId, Long memId, Long ordId) {
        Shipment shipment = shipmentRepository.getByIdOrThrow(shId);

        validateShipmentAccess(shipment, ordId, memId);

        return mapToShipmentDto(shipment);
    }

    public void updateStatus(Long shId, Long memId, Long ordId) {
        Shipment shipment = shipmentRepository.getByIdOrThrow(shId);

        validateShipmentAccess(shipment, ordId, memId);

        switch (shipment.getShStt()) {
            case PREPARING -> shipment.setShStt(ShipmentStatus.SHIPPING);
            case SHIPPING -> shipment.setShStt(ShipmentStatus.DELIVERING);
            case DELIVERING -> shipment.setShStt(ShipmentStatus.DELIVERED);
            default -> throw new IllegalStateException("다음 배송 단계로 자동 업데이트할 수 없는 상태입니다.");
        }

        syncOrderStatus(shipment.getOrder());
    }

    public ShipmentDto updateStatusFromAdmin(Long shId, Long ordId, ShipmentDto dto) {
        Shipment shipment = shipmentRepository.getByIdOrThrow(shId);

        if (!shipment.getOrder().getOrdBaseId().equals(ordId)) throw new IllegalArgumentException("해당 주문의 배송 내역이 아닙니다.");

        shipment.setShStt(dto.getShStt());

        syncOrderStatus(shipment.getOrder());

        return mapToShipmentDto(shipment);
    }

    private void syncOrderStatus(Order order) {
        if (order.getOrdBaseStt() == OrderStatus.CANCELED) return;

        List<Shipment> activeShipments = order.getShipments().stream()
                .filter(sh -> !sh.getShCanYn())
                .toList();

        if (activeShipments.isEmpty()) return;

        long deliveredCount = activeShipments.stream()
                .filter(sh -> sh.getShStt() == ShipmentStatus.DELIVERED)
                .count();

        boolean isDelivering = activeShipments.stream()
                .anyMatch(sh -> sh.getShStt() == ShipmentStatus.DELIVERING || sh.getShStt() == ShipmentStatus.SHIPPING);

        if (deliveredCount == activeShipments.size()) {  // 모든 배송이 배송 완료된 경우
            order.setOrdBaseStt(OrderStatus.DELIVERED);
        }
        else if (isDelivering || deliveredCount > 0) {  // 배송이 1개라도 배송중이라면
            order.setOrdBaseStt(OrderStatus.DELIVERING);
        }
        else { // 그 외는 상품 준비 단계로
            order.setOrdBaseStt(OrderStatus.PREPARING);
        }
    }

    private ShipmentDto mapToShipmentDto(Shipment shipment){
        return ShipmentDto.builder()
                .shId(shipment.getShId())
                .shCarCd(shipment.getShCarCd())
                .shStt(shipment.getShStt())
                .shCreDt(shipment.getShCreDt())
                .shTraNo(shipment.getShTraNo())
                .shRcvNm(shipment.getShRcvNm())
                .shAdr(shipment.getShAdr())
                .shAdrDt(shipment.getShAdrDt())
                .build();
    }


    private void validateShipmentAccess(Shipment shipment, Long ordId, Long memId) {
        if (!shipment.getOrder().getOrdBaseId().equals(ordId)) {
            throw new IllegalArgumentException("해당 주문의 배송 내역이 아닙니다.");
        }
        if (!shipment.getOrder().getMember().getMemId().equals(memId)) {
            throw new IllegalArgumentException("본인의 주문 배송 내역만 제어할 수 있습니다.");
        }
    }
}
