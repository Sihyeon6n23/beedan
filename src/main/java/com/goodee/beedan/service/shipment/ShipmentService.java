package com.goodee.beedan.service.shipment;

import com.goodee.beedan.common.constant.OrderStatus;
import com.goodee.beedan.common.constant.ShipmentStatus;
import com.goodee.beedan.dto.order.ShipmentDto;
import com.goodee.beedan.entity.Order;
import com.goodee.beedan.entity.Shipment;
import com.goodee.beedan.repository.order.OrderRepository;
import com.goodee.beedan.repository.order.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ShipmentService {
    private final ShipmentRepository shipmentRepository;
    private final OrderRepository orderRepository;

    public List<ShipmentDto> getShipmentList(Long shId, Long memId, Long ordId){
        orderRepository.findById(ordId).orElseThrow(()->new IllegalArgumentException("주문의 배송 내역을 찾을 수 없습니다."));

        List<ShipmentDto> shipmentDtoList = shipmentRepository.findByOrder_OrdBaseIdOrderByShCreDtDesc(ordId).stream()
                .map(shipment -> mapToShipmentDto(shipment))
                .toList();

        return shipmentDtoList;
    }

    public ShipmentDto getShipmentDetail(Long shId, Long memId, Long ordId){
        Shipment shipment = shipmentRepository.findById(shId)
                .orElseThrow(()->new IllegalArgumentException("배송 내역을 찾을 수 없습니다."));

        if(!shipment.getOrder().getOrdBaseId().equals(ordId)) {
            throw new IllegalArgumentException("주문의 배송 내역이 아닙니다.");
        }

        if(!shipment.getOrder().getMember().getMemId().equals(memId)) {
            throw new IllegalArgumentException("본인의 주문 배송 내역만 조회할 수 있습니다.");
        }

        return mapToShipmentDto(shipment);
    }

    public void updateStaus(Long shId, Long memId, Long ordId, ShipmentDto dto){ // 다음 배송 단계로 업데이트
        Order order = orderRepository.findById(ordId).orElseThrow(()->new IllegalArgumentException("주문을 찾을 수 없습니다."));
        Shipment shipment = shipmentRepository.findById(shId).orElseThrow(()->new IllegalArgumentException("배송 내역을 찾을 수 없습니다."));

        if(!shipment.getOrder().getOrdBaseId().equals(ordId)) {
            throw new IllegalArgumentException("주문의 배송 내역이 아닙니다.");
        }

        if(!shipment.getOrder().getMember().getMemId().equals(memId)) {
            throw new IllegalArgumentException("본인의 주문 배송 내역만 조회할 수 있습니다.");
        }

        if(!order.getOrdBaseStt().equals(OrderStatus.DELIVERING) || !order.getOrdBaseStt().equals(OrderStatus.PREPARING)) return;
        if(dto.getShStt() == null) return;

        switch (shipment.getShStt()){
            case SHIPPING -> {shipment.setShStt(ShipmentStatus.CUSTOMS); break;}
            case CUSTOMS -> {shipment.setShStt(ShipmentStatus.DELIVERING);break;}
            case DELIVERING -> {shipment.setShStt(ShipmentStatus.DELIVERING);break;}
            default -> throw new IllegalStateException("배송 상태를 업데이트할 수 없습니다.");
        }
        shipmentRepository.save(shipment);
    }

    public ShipmentDto updateStatusFromAdmin(Long shId, Long memId, Long ordId, ShipmentDto dto){  // 관리지가 직접 배송 현황을 수정하는 경우(테스트용)
        Shipment shipment = shipmentRepository.findById(shId).orElseThrow(()->new IllegalArgumentException("배송 내역을 찾을 수 없습니다."));

        if(!shipment.getOrder().getOrdBaseId().equals(ordId)) {
            throw new IllegalArgumentException("주문의 배송 내역이 아닙니다.");
        }

        Order order = orderRepository.findById(ordId).orElseThrow(()->new IllegalArgumentException("주문을 찾을 수 없습니다."));

        switch (shipment.getShStt()) {
            case SHIPPING -> {shipment.setShStt(ShipmentStatus.SHIPPING); break;}
            case CUSTOMS -> {shipment.setShStt(ShipmentStatus.CUSTOMS); break;}
            case DELIVERING -> {
                shipment.setShStt(ShipmentStatus.DELIVERING);
                order.setOrdBaseStt(OrderStatus.DELIVERING);
                break;
            }
            case DELIVERED -> {
                shipment.setShStt(ShipmentStatus.DELIVERED);
                order.setOrdBaseStt(OrderStatus.DELIVERED);
                break;
            }
            case DELAYED -> {shipment.setShStt(ShipmentStatus.DELAYED);break;}
            case RETURED -> {
                shipment.setShStt(ShipmentStatus.RETURED);
                order.setOrdBaseStt(OrderStatus.CANCELLED);
                break;
            }
            default -> throw new IllegalStateException("배송 상태를 업데이트할 수 없습니다.");
        }
        shipmentRepository.save(shipment);

        dto.setShStt(shipment.getShStt());

        return dto;
    }

    public void cancelShipment(Long shId, Long memId, Long ordId, ShipmentStatus shStt){ // 세관을 통과하지 못해 국내 배송 이전에 취소되는 경우
        Order order = orderRepository.findById(ordId).orElseThrow(()->new IllegalArgumentException("주문을 찾을 수 없습니다."));
        Shipment shipment = shipmentRepository.findById(shId).orElseThrow(()->new IllegalArgumentException("배송 내역을 찾을 수 없습니다."));

        if(!shipment.getOrder().getOrdBaseId().equals(ordId)) {
            throw new IllegalArgumentException("주문의 배송 내역이 아닙니다.");
        }

        if(!shipment.getOrder().getMember().getMemId().equals(memId)) {
            throw new IllegalArgumentException("본인의 주문 배송 내역만 조회할 수 있습니다.");
        }

        if(shStt.equals(ShipmentStatus.RETURED)) {
            shipment.setShStt(ShipmentStatus.RETURED);
            order.setOrdBaseStt(OrderStatus.CANCELLED);
        }

        shipmentRepository.save(shipment);
        orderRepository.save(order);
    }

    public void createOrder(Long ordId) {

    }

    public ShipmentDto mapToShipmentDto(Shipment shipment){
        ShipmentDto shipmentDto = ShipmentDto.builder()
                .shId(shipment.getShId())
                .shCarCd(shipment.getShCarCd())
                .shStt(shipment.getShStt())
                .shCreDt(shipment.getShCreDt())
                .shTraNo(shipment.getShTraNo())
                .build();

        return shipmentDto;
    }
}
