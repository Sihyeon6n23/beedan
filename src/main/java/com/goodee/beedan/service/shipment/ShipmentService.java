package com.goodee.beedan.service.shipment;

import com.goodee.beedan.common.constant.OrderStatus;
import com.goodee.beedan.common.constant.ShipmentStatus;
import com.goodee.beedan.dto.order.ShipmentDto;
import com.goodee.beedan.entity.Order;
import com.goodee.beedan.entity.Shipment;
import com.goodee.beedan.repository.order.OrderRepository;
import com.goodee.beedan.repository.order.ShipmentRepository;
import com.goodee.beedan.service.order.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional // 기본적으로 쓰기(Write) 트랜잭션 적용
public class ShipmentService {
    private final ShipmentRepository shipmentRepository;
    private final OrderRepository orderRepository;
    private final OrderService orderService;

    @Transactional(readOnly = true)
    public List<ShipmentDto> getShipmentList(Long memId, Long ordId) {
        Order order = orderRepository.findById(ordId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));

        if (!order.getMember().getMemId().equals(memId)) {
            throw new IllegalArgumentException("본인의 주문 배송 목록만 조회할 수 있습니다.");
        }

        return shipmentRepository.findByOrder_OrdBaseIdOrderByShCreDtDesc(ordId).stream()
                .map(this::mapToShipmentDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public ShipmentDto getShipmentDetail(Long shId, Long memId, Long ordId) {
        Shipment shipment = shipmentRepository.findById(shId)
                .orElseThrow(() -> new IllegalArgumentException("배송 내역을 찾을 수 없습니다."));

        validateShipmentAccess(shipment, ordId, memId);

        return mapToShipmentDto(shipment);
    }

    public void updateStatus(Long shId, Long memId, Long ordId) {
        Shipment shipment = shipmentRepository.findById(shId)
                .orElseThrow(() -> new IllegalArgumentException("배송 내역을 찾을 수 없습니다."));
        Order order = shipment.getOrder();

        validateShipmentAccess(shipment, ordId, memId);

        if (!order.getOrdBaseStt().equals(OrderStatus.DELIVERING) && !order.getOrdBaseStt().equals(OrderStatus.PREPARING)) {
            return;
        }

        switch (shipment.getShStt()) {
            case PREPARING -> shipment.setShStt(ShipmentStatus.SHIPPING);
            case SHIPPING -> shipment.setShStt(ShipmentStatus.DELIVERING);
            case DELIVERING -> shipment.setShStt(ShipmentStatus.DELIVERED);
            default -> throw new IllegalStateException("다음 배송 단계로 자동 업데이트할 수 없는 상태입니다.");
        }
    }

    public ShipmentDto updateStatusFromAdmin(Long shId, Long ordId, ShipmentDto dto) {
        Shipment shipment = shipmentRepository.findById(shId)
                .orElseThrow(() -> new IllegalArgumentException("배송 내역을 찾을 수 없습니다."));

        if (!shipment.getOrder().getOrdBaseId().equals(ordId)) {
            throw new IllegalArgumentException("해당 주문의 배송 내역이 아닙니다.");
        }

        ShipmentStatus newStatus = dto.getShStt();
        shipment.setShStt(newStatus);

        if (newStatus == ShipmentStatus.DELIVERING) {
            orderService.updateOrderStatus(ordId, OrderStatus.DELIVERING);
        } else if (newStatus == ShipmentStatus.DELIVERED) {
            orderService.updateOrderStatus(ordId, OrderStatus.DELIVERED);
        } else if (newStatus == ShipmentStatus.PREPARING) {
            orderService.updateOrderStatus(ordId, OrderStatus.PREPARING);
        }

        dto.setShStt(shipment.getShStt());
        return dto;
    }

    public void cancelShipment(Long shId, Long memId, Long ordId) { //
        Shipment shipment = shipmentRepository.findById(shId)
                .orElseThrow(() -> new IllegalArgumentException("배송 내역을 찾을 수 없습니다."));

        Order order = orderRepository.findById(ordId).orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));

        validateShipmentAccess(shipment, ordId, memId);

        if (order.getOrdBaseStt() == OrderStatus.CANCELLED) {
            shipment.setSh_can_yn(true);
            orderService.cancelOrder(ordId, memId);
        }
    }

    public ShipmentDto mapToShipmentDto(Shipment shipment){
        return ShipmentDto.builder()
                .shId(shipment.getShId())
                .shCarCd(shipment.getShCarCd())
                .shStt(shipment.getShStt())
                .shCreDt(shipment.getShCreDt())
                .shTraNo(shipment.getShTraNo())
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
