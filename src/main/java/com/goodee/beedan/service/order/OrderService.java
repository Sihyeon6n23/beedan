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
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
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
    public void updateOrder(Long ordId, Long memId, OrderDto dto){
        Order order = orderRepository.findById(ordId).orElseThrow(()->new IllegalArgumentException("Order not found"));

        if(!order.getMember().getMemId().equals(memId)) {
            throw new IllegalArgumentException("본인의 주문만 수정할 수 있습니다.");
        }

        if (order.getOrdBaseStt() != OrderStatus.PREPARING) {
            throw new IllegalStateException("배송 준비 중일 때만 주소를 수정할 수 있습니다.");
        }
        if(dto == null) return;

        if(dto.getOrdBaseAdr() != null) order.setOrdBaseAdr(dto.getOrdBaseAdr());
        if(dto.getOrdBaseAdrDt() != null) order.setOrdBaseAdrDt(dto.getOrdBaseAdrDt());
        if(dto.getOrdBaseRcvNm() != null) order.setOrdBaseRcvNm(dto.getOrdBaseRcvNm());
        if(dto.getOrdBaseMsg() != null) order.setOrdBaseMsg(dto.getOrdBaseMsg());
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
    }

    @Transactional
    public void createOrder(Long memId, OrderDto dto){
        Member member = memberRepository.findById(memId).orElseThrow(()->new UsernameNotFoundException("User not found"));

        // 1. 협상 정보 조회
        Negotiation negotiation = negotiationRepository.findFirstByMemIdOrderByNgCreDtDesc(memId);
        if (negotiation == null) {
            throw new IllegalStateException("해당 회원의 협상 정보가 없습니다.");
        }

        // 2. 견적 상세 정보 조회
        List<QuoteDetail> quoteDetails = quoteDetailRepository.findAllByNgId(negotiation.getNgId());
        if (quoteDetails.isEmpty()) {
            throw new IllegalStateException("견적 상세 상품이 존재하지 않습니다.");
        }

        // 3. 수량 및 금액 계산 로직 수정 (size() -> 수량 합산)
        Integer totalQuantity = quoteDetails.stream()
                .mapToInt(QuoteDetail::getQuDtQn)
                .sum();

        // 주문 생성
        Order order = Order.builder()
                .member(member)
                .ordBaseRcvNm(dto.getOrdBaseRcvNm())
                .ordBaseAdr(dto.getOrdBaseAdr())
                .ordBaseAdrDt(dto.getOrdBaseAdrDt())
                .ordBaseMsg(dto.getOrdBaseMsg())
                .ordBaseStt(OrderStatus.PREPARING)
                .ordBaseNo(negotiation.getNgNm()) // 협상 이름을 주문 번호로 사용
                .build();
        orderRepository.save(order);

        log.info("주문 생성 성공");

        // 주문 상품 생성
        OrderItem orderItem = OrderItem.builder()
                .order(order)
                .ordItemQn(totalQuantity) // 총 주문 수량
                .build();
        orderItemRepository.save(orderItem);

        log.info("주문 상품 생성 성공");

        // 배송 생성
        Shipment shipment = Shipment.builder()
                .order(order)
                .shStt(ShipmentStatus.PREPARING)
                .shCarCd(createRandNum(2)) // 랜덤 배송사 번호
                .shTraNo(createRandNum(1)) // 랜덤 운송장 번호
                .build();
        shipmentRepository.save(shipment);

        log.info("배송 생성 성공");

        // 배송 아이템 생성
        List<ShipmentItem> shipmentItems = new ArrayList<>();
        for (QuoteDetail quoteDetail : quoteDetails) {
            ShipmentItem shipmentItem = ShipmentItem.builder()
                    .shipment(shipment)
                    .orderItem(orderItem)
                    .shQn(quoteDetail.getQuDtQn())
                    .shItemStt(ShipmentStatus.PREPARING)
                    .build();
            shipmentItems.add(shipmentItem);
        }
        shipmentItemRepository.saveAll(shipmentItems);

        log.info("배송 아이템 생성 성공");
    }

    public OrderDto mapToOrderDto(Order order){
        OrderDto orderDto = OrderDto.builder()
                .ordBaseId(order.getOrdBaseId())
                .ordBaseAdr(order.getOrdBaseAdr())
                .ordBaseAdrDt(order.getOrdBaseAdrDt())
                .ordBaseRcvNm(order.getOrdBaseRcvNm())
                .ordBaseMsg(order.getOrdBaseMsg())
                .ordBaseStt(order.getOrdBaseStt())
                .ordBaseNo(order.getOrdBaseNo())
                .ordBaseCreDt(order.getOrdBaseCreDt())
                .build();

        return orderDto;
    }

    public String createRandNum(int caseCd){
        Random random = new Random();

        if(caseCd == 1){
        return IntStream.range(0, 12)
                .mapToObj(i -> String.valueOf(random.nextInt(10)))
                .collect(Collectors.joining());
        } else {
            return IntStream.range(0, 3)
                    .mapToObj(i -> String.valueOf(random.nextInt(10)))
                    .collect(Collectors.joining());
        }
    }

}
