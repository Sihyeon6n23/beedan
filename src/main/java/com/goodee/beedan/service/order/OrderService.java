package com.goodee.beedan.service.order;

import com.goodee.beedan.common.constant.OrderStatus;
import com.goodee.beedan.common.constant.ShipmentStatus;
import com.goodee.beedan.dto.order.OrderDto;
import com.goodee.beedan.entity.*;
import com.goodee.beedan.repository.member.MemberRepository;
import com.goodee.beedan.repository.order.OrderRepository;
import com.goodee.beedan.repository.order.ShipmentRepository;
import com.goodee.beedan.repository.quote.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class OrderService {
    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;

    private final QuoteDetailRepository quoteDetailRepository;
    private final NegotiationRepository negotiationRepository;

    private final ShipmentRepository shipmentRepository;
    private final ShipmentItemRepository shipmentItemRepository;
    private final OrderItemRepository orderItemRepository;
    private final QuoteInfoRepository  quoteInfoRepository;

    public List<OrderDto> getOrderList(Long memId){
        if(!memberRepository.existsById(memId)) return null;

        List<OrderDto> orderList = orderRepository.findByMember_MemIdOrderByOrdBaseCreDtDesc(memId).stream()
                .map(order -> mapToOrderDto(order))
                .toList();

        return orderList;
    }

    public OrderDto getOrderDetail(Long ordId, Long memId){
        Order order = orderRepository.findById(ordId)
                .orElseThrow(()->new IllegalArgumentException("주문을 찾을 수 없습니다."));

        if(!order.getMember().getMemId().equals(memId)) {
            throw new IllegalArgumentException("본인의 주문만 조회할 수 있습니다.");
        }

        return mapToOrderDto(order);
    }

    @Transactional
    public void updateOrder(Long ordId, Long memId, OrderDto dto) {
        Order order = orderRepository.findById(ordId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        if (!order.getMember().getMemId().equals(memId)) {
            throw new IllegalArgumentException("본인의 주문만 수정할 수 있습니다.");
        }

        if (order.getOrdBaseStt() != OrderStatus.PREPARING) {
            throw new IllegalStateException("배송 준비 중일 때만 정보를 수정할 수 있습니다.");
        }

        if (dto == null) return;

        if (dto.getOrdBaseAdr() != null) order.setOrdBaseAdr(dto.getOrdBaseAdr());
        if (dto.getOrdBaseAdrDt() != null) order.setOrdBaseAdrDt(dto.getOrdBaseAdrDt());
        if (dto.getOrdBaseRcvNm() != null) order.setOrdBaseRcvNm(dto.getOrdBaseRcvNm());
        if (dto.getOrdBaseMsg() != null) order.setOrdBaseMsg(dto.getOrdBaseMsg());
    }
    @Transactional
    public void updateOrderStatus(Long ordId, OrderStatus newStatus) {
        Order order = orderRepository.findById(ordId).orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));

        if(order.getOrdBaseStt() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("취소된 주문의 상태는 변경할 수 없습니다.");
        }

        order.setOrdBaseStt(newStatus);
    }

    @Transactional
    public void cancelOrder(Long ordId, Long memId) {
        if(!memberRepository.existsById(memId)) throw new IllegalArgumentException("회원이 존재하지 않습니다.");

        Order order = orderRepository.findById(ordId).orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));

        if (order.getOrdBaseStt() == OrderStatus.DELIVERING || order.getOrdBaseStt() == OrderStatus.DELIVERED) {
            throw new IllegalStateException("이미 배송이 시작되어 취소할 수 없습니다.");
        }

        order.setOrdBaseStt(OrderStatus.CANCELLED);

        order.getShipments().forEach(sh -> sh.setShCanYn(true));
    }

    @Transactional
    public void createOrder(Long memId, OrderDto dto) {
        Member member = memberRepository.findById(memId).orElseThrow(()->new UsernameNotFoundException("User not found"));

        Negotiation negotiation = negotiationRepository.findFirstByMemIdOrderByNgCreDtDesc(memId);
        if (negotiation == null) {
            throw new IllegalStateException("해당 회원의 협상 정보가 없습니다."); // 협상 이름은 주문번호로 사용
        }

        List<QuoteDetail> quoteDetails = quoteDetailRepository.findAllByNgId(negotiation.getNgId());
        if (quoteDetails.isEmpty()) {
            throw new IllegalStateException("견적 상세 상품이 존재하지 않습니다.");
        }

        Integer totalQuantity = quoteDetails.stream()
                .mapToInt(QuoteDetail::getQuDtQn)
                .sum();

        BigDecimal bigDecimal = quoteInfoRepository.findFirstByNgIdOrderByQuInfoIdDesc(negotiation.getNgId())
                .map(QuoteInfo::getQuInfoTp)
                .orElse(BigDecimal.ZERO);

        Order order = Order.builder()
                .member(member)
                .ordBaseRcvNm(dto.getOrdBaseRcvNm())
                .ordBaseAdr(dto.getOrdBaseAdr())
                .ordBaseAdrDt(dto.getOrdBaseAdrDt())
                .ordBaseMsg(dto.getOrdBaseMsg())
                .ordBaseStt(OrderStatus.PREPARING)
                .ordBaseTtAm(bigDecimal.multiply(BigDecimal.valueOf(totalQuantity)))
                .ordBaseNo(negotiation.getNgNm())
                .build();
        orderRepository.save(order);

        Map<Long, OrderItem> orderItemMap = new HashMap<>(); // 전체 수량을 QuoteDetail별로 저장(상품 수량, 이름)
        for (QuoteDetail quoteDetail : quoteDetails) {
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .ordItemQn(quoteDetail.getQuDtQn())
                    .ordItemStNm(quoteDetail.getStNm())
                    .build();
            orderItemRepository.save(orderItem);

            orderItemMap.put(quoteDetail.getQuDtId(), orderItem); // ShipmentItem과 연결하기 위해 견적상세ID를 Key로 담아둠
        }

        if (dto.getShipmentRequests() != null && !dto.getShipmentRequests().isEmpty()) {
            for (OrderDto.ShipmentRequestDto shipmentDto : dto.getShipmentRequests()) {

                Shipment shipment = Shipment.builder()
                        .order(order)
                        .shRcvNm(shipmentDto.getShRcvNm())
                        .shAdr(shipmentDto.getShAdr())
                        .shAdrDt(shipmentDto.getShAdrDt())
                        .shStt(ShipmentStatus.PREPARING)
                        .shCarCd(createRandNum(2))
                        .shTraNo(createRandNum(1))
                        .build();
                shipmentRepository.save(shipment);

                List<ShipmentItem> shipmentItems = new ArrayList<>();
                for (OrderDto.ShipmentItemRequestDto itemDto : shipmentDto.getItems()) {
                    OrderItem targetOrderItem = orderItemMap.get(itemDto.getQuDtId());

                    if (targetOrderItem == null) {
                        throw new IllegalStateException("유효하지 않은 견적 상품 ID입니다.");
                    }

                    ShipmentItem shipmentItem = ShipmentItem.builder()
                            .shipment(shipment)
                            .orderItem(targetOrderItem)
                            .shQn(itemDto.getShQn())
                            .build();
                    shipmentItems.add(shipmentItem);
                }
                shipmentItemRepository.saveAll(shipmentItems);
            }
        }
    }

    public OrderDto mapToOrderDto(Order order) {
        OrderDto orderDto = OrderDto.builder()
                .ordBaseId(order.getOrdBaseId())
                .ordBaseNo(order.getOrdBaseNo())
                .ordBaseRcvNm(order.getOrdBaseRcvNm())
                .ordBaseAdr(order.getOrdBaseAdr())
                .ordBaseAdrDt(order.getOrdBaseAdrDt())
                .ordBaseMsg(order.getOrdBaseMsg())
                .ordBaseStt(order.getOrdBaseStt())
                .ordBaseTtAm(order.getOrdBaseTtAm())
                .ordBaseCreDt(order.getOrdBaseCreDt())
                .build();

        if (order.getShipments() != null) {
            List<OrderDto.ShipmentResponseDto> shipmentDtos = order.getShipments().stream()
                    .map(this::mapToShipmentDto) // 하위 변환 메서드 호출
                    .toList();
            orderDto.setShipmentResponses(shipmentDtos);
        }

        return orderDto;
    }

    // Shipment 엔티티를 DTO로 변환하는 보조 메서드
    private OrderDto.ShipmentResponseDto mapToShipmentDto(Shipment shipment) {
        return OrderDto.ShipmentResponseDto.builder()
                .shId(shipment.getShId())
                .shRcvNm(shipment.getShRcvNm())
                .shAdr(shipment.getShAdr())
                .shAdrDt(shipment.getShAdrDt())
                .shTraNo(shipment.getShTraNo())
                .shCarCd(shipment.getShCarCd())
                .shStt(shipment.getShStt())
                .shMsg(shipment.getShMsg())
                .shipmentItems(shipment.getShipmentItems().stream()
                        .map(si -> OrderDto.ShipmentItemResponseDto.builder()
                                .shItemId(si.getShItemId())
                                .shQn(si.getShQn())
                                // 필요시 OrderItem을 통해 상품명 등을 추가로 가져옴
                                .build())
                        .toList())
                .build();
    }

    public String createRandNum(int caseCd){
        Random random = new Random();

        if(caseCd == 1){
        return IntStream.range(0, 12)
                    .mapToObj(i -> String.valueOf(ThreadLocalRandom.current().nextInt(10)))
                    .collect(Collectors.joining());
        } else {
            return IntStream.range(0, 3)
                    .mapToObj(i -> String.valueOf(random.nextInt(10)))
                    .collect(Collectors.joining());
        }
    }

}
