package com.goodee.beedan.service.order;

import com.goodee.beedan.common.constant.MemberAuthority;
import com.goodee.beedan.common.constant.OrderStatus;
import com.goodee.beedan.common.constant.ShipmentStatus;
import com.goodee.beedan.config.security.MemberUserDetails;
import com.goodee.beedan.dto.order.OrderDto;
import com.goodee.beedan.dto.order.WebhookShipmentRequest;
import com.goodee.beedan.entity.*;
import com.goodee.beedan.repository.member.MemberRepository;
import com.goodee.beedan.repository.order.OrderRepository;
import com.goodee.beedan.repository.order.ShipmentRepository;
import com.goodee.beedan.repository.payment.PaymentRepository;
import com.goodee.beedan.repository.quote.*;
import com.goodee.beedan.repository.stock.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final PaymentRepository paymentRepository;
    private final QuoteBaseRepository quoteBaseRepository;
    private final StockRepository stockRepository;

    private final ThumbnailRedisService thumbnailRedisService;

    public Page<OrderDto> getOrderList(Long memId, Pageable pageable){
        if(!memberRepository.existsById(memId)) return null;

        Page<Order> orderList = orderRepository.findByMember_MemIdOrderByOrdBaseCreDtDesc(memId, pageable);

        return orderList.map(this::mapToOrderDto);
    }

    @Transactional(readOnly = true)
    public OrderDto getOrderDetail(Long ordId, MemberUserDetails userDetails) {
        Order order = orderRepository.findByIdWithShipments(ordId).orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));

        boolean isAdmin = userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isOwner = order.getMember().getMemId().equals(userDetails.getMemberId());

        if (!isAdmin && !isOwner) throw new IllegalArgumentException("해당 주문에 대한 조회 권한이 없습니다.");
        
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
        Order order = orderRepository.findById(ordId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다. ID: " + ordId));

        if (order.getOrdBaseStt() == newStatus) return; // 같은 상태 선택시 상태 변경 방지

        if (order.getOrdBaseStt() == OrderStatus.CANCELED) {
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
        order.setOrdBaseStt(OrderStatus.CANCELED);
        order.getShipments().forEach(sh -> sh.setShCanYn(true));
    }

    @Transactional
    public void createOrderFromWebhook(WebhookShipmentRequest webhookRequest) {
        // quId (QouteBase) 사용 Payment 총 결제금액 확인
        // quId 사용 QuoteDetail에서 주문한 상품 목록 및 배송지 정보 (수령인, 주소)
        // memId가 들어가 있는 곳은 negotiation
        QuoteBase quoteBase = quoteBaseRepository.findById(webhookRequest.getQuId()).orElseThrow(() -> new IllegalStateException("견적 정보가 없습니다."));
        Negotiation negotiation = negotiationRepository.findByNgId(quoteBase.getNgId());
        Member member = memberRepository.findById(negotiation.getMemId()).orElseThrow(()-> new UsernameNotFoundException("일치하는 회원이 없습니다."));
        Payment payment = paymentRepository.findByQuId(quoteBase.getQuId()).orElseThrow(()->new IllegalArgumentException("결제 정보가 없습니다"));
        List<QuoteDetail> quoteDetails = quoteDetailRepository.findAllByQuId(quoteBase.getQuId());

        if (quoteDetails.isEmpty()) throw new IllegalStateException("견적 상세 상품이 없습니다.");

        QuoteDetail firstItem = quoteDetails.get(0);
        Order order = Order.builder()
                .member(member)
                .ordBaseStt(OrderStatus.PREPARING)
                .ordBaseRcvNm(firstItem.getQuDtRcNm())
                .ordBaseTtAm(payment.getPyTtAm())
                .ordBaseNo(quoteBase.getQuCd())
                .build();
        orderRepository.save(order);

        Map<String, List<QuoteDetail>> groupedByAddress = quoteDetails.stream()
                .collect(Collectors.groupingBy(d -> d.getQuDtRcNm() + "_" + d.getQuDtRcAdr()));

        // Shipment 및 관련 아이템 생성
        for (Map.Entry<String, List<QuoteDetail>> entry : groupedByAddress.entrySet()) {
            List<QuoteDetail> groupItems = entry.getValue();
            QuoteDetail addressInfo = groupItems.get(0); // 그룹의 대표 배송지 정보

            // 목적지별 Shipment 생성
            Shipment shipment = Shipment.builder()
                    .order(order)
                    .shRcvNm(addressInfo.getQuDtRcNm())
                    .shAdr(addressInfo.getQuDtRcAdr())
                    .shAdrDt(addressInfo.getQuDtRcPhn())
                    .shStt(ShipmentStatus.PREPARING)
                    .shCarCd(webhookRequest.getShCarNo())
                    .shTraNo(webhookRequest.getShTraNo())
                    .shHblNo(webhookRequest.getShHblNo())
                    .shCanYn(false)
                    .build();
            shipmentRepository.save(shipment);

            // 주문 상품, 배송 물품
            for (QuoteDetail detail : groupItems) {
                Stock stock = stockRepository.findById(detail.getStId()).orElseThrow(()->new IllegalArgumentException("상품 정보가 없습니다."));
                String thumbKey = "display:thumbnail:" + UUID.randomUUID().toString();

                OrderItem orderItem = OrderItem.builder()
                        .order(order)
                        .ordItmQn(detail.getQuDtQn())
                        .ordItmNm(detail.getStNm())
                        .ordItmThumbKey(thumbKey)
                        .ordItmStUrl(stock.getStImgUrl())
                        .build();
                orderItemRepository.save(orderItem);

                thumbnailRedisService.generateAndCache(thumbKey, stock.getStImgUrl());

                // ShipmentItem 생성 (Shipment와 OrderItem 연결)
                ShipmentItem shipmentItem = ShipmentItem.builder()
                        .shipment(shipment)
                        .orderItem(orderItem)
                        .shQn(detail.getQuDtQn())
                        .ordItmNm(detail.getStNm())
                        .build();
                shipmentItemRepository.save(shipmentItem);
            }
        }

    }

    public OrderDto mapToOrderDto(Order order) {
        String summaryName = "상품 없음";
        String repThumbUrl = null;
        List<OrderDto.OrderItemResponseDto> orderItemDtos = new ArrayList<>(); // 초기화

        List<OrderItem> orderItems = order.getOrderItems();

        // 주문 상품이 여러개 있는 경우 (대표명, 대표 썸네일, 개별 DTO 변환을 한 번에 처리)
        if (orderItems != null && !orderItems.isEmpty()) {
            int totalItems = orderItems.size();
            OrderItem firstItem = orderItems.get(0);
            String firstItemName = firstItem.getOrdItmNm();

            summaryName = (totalItems > 1) ? firstItemName + " 외 " + (totalItems - 1) + "건" : firstItemName;

            if (firstItem.getOrdItmThumbKey() != null) {
                repThumbUrl = "/api/images/thumb/" + firstItem.getOrdItmThumbKey();
            }

            orderItemDtos = orderItems.stream()
                    .map(item -> OrderDto.OrderItemResponseDto.builder()
                            .ordItmNm(item.getOrdItmNm())
                            .ordItmQn(item.getOrdItmQn())
                            .ordItmThumbKey(item.getOrdItmThumbKey())
                            .ordItmThumbUrl(item.getOrdItmThumbKey() != null ? "/api/images/thumb/" + item.getOrdItmThumbKey() : null)
                            .build())
                    .collect(Collectors.toList());
        }

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
                .ordSummaryNm(summaryName)
                .ordThumbUrl(repThumbUrl)
                .orderItems(orderItemDtos)
                .build();

        if (order.getShipments() != null) {
            List<OrderDto.ShipmentResponseDto> shipmentDtos = order.getShipments().stream()
                    .map(this::mapToShipmentDto)
                    .toList();
            orderDto.setShipmentResponses(shipmentDtos);
        }

        return orderDto;
    }

    private OrderDto.ShipmentResponseDto mapToShipmentDto(Shipment shipment) {
        List<OrderDto.ShipmentItemResponseDto> shipmentItemDtos = shipment.getShipmentItems().stream()
                .map(shItem -> OrderDto.ShipmentItemResponseDto.builder()
                        .ordItmNm(shItem.getOrdItmNm()) // ShipmentItem 엔티티의 상품명 바로 사용
                        .shQn(shItem.getShQn())         // ShipmentItem 엔티티의 배송 수량 바로 사용
                        .build())
                .toList();

        return OrderDto.ShipmentResponseDto.builder()
                .shId(shipment.getShId())
                .shTraNo(shipment.getShTraNo())
                .shCarCd(shipment.getShCarCd())
                .shStt(shipment.getShStt())
                .shRcvNm(shipment.getShRcvNm())
                .shAdr(shipment.getShAdr())
                .shAdrDt(shipment.getShAdrDt())
                .shMsg(shipment.getShMsg())
                .shCanYn(shipment.getShCanYn())
                .shCusStt(shipment.getShCusStt())
                .shipmentItems(shipmentItemDtos)
                .build();
    }

    @Transactional(readOnly = true)
    public Page<OrderDto> getListByAdmin(Long adminMemId, Pageable pageable) {
        Member member = memberRepository.findById(adminMemId).orElseThrow(()->new UsernameNotFoundException("User not found"));
        if(!member.getMemAut().equals(MemberAuthority.ADMIN)) throw new IllegalArgumentException("관리자만 주문 목록을 조회할 수 있습니다.");

        Page<Order> orderPage = orderRepository.findAll(pageable);

        return orderPage.map(this::mapToOrderDto);
    }


}
