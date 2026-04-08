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
    private final QuoteInfoRepository  quoteInfoRepository;
    private final PaymentRepository paymentRepository;
    private final QuoteBaseRepository quoteBaseRepository;

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
    public String createOrderFromWebhook(WebhookShipmentRequest webhookRequest) {
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
                .ordBaseAdr(firstItem.getQuDtRcAdr())
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
                    .shAdrDt(addressInfo.getQuDtRcPhn()) // DB 컬럼 상황에 맞춰 전화번호나 상세주소 매핑
                    .shStt(ShipmentStatus.PREPARING)
                    .shCarCd(webhookRequest.getShCarNo())    // 해외 물류사 코드
                    .shTraNo(webhookRequest.getShTraNo())    // 해외 통합 송장 번호
                    .shCanYn(false)
                    .build();
            shipmentRepository.save(shipment);

            // Shipment에 속한 상품들 처리
            for (QuoteDetail detail : groupItems) {
                OrderItem orderItem = OrderItem.builder()
                        .order(order)
                        .ordItmQn(detail.getQuDtQn())
                        .ordItmNm(detail.getStNm())
                        .build();
                orderItemRepository.save(orderItem);

                // ShipmentItem 생성 (Shipment와 OrderItem 연결)
                ShipmentItem shipmentItem = ShipmentItem.builder()
                        .shipment(shipment)
                        .orderItem(orderItem)
                        .shQn(detail.getQuDtQn())
                        .ordItmNm(detail.getStNm()) // 추후 국내 배송 시 업데이트될 필드
                        .build();
                shipmentItemRepository.save(shipmentItem);
            }
        }

        log.info("주문 생성 성공: {}", order.getOrdBaseId());
        return "주문 생성 완료";
    }

    @Transactional
    public Long createOrder(Long memId, OrderDto dto) {
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
                .ordBaseStt(OrderStatus.PREPARING)
                .ordBaseRcvNm(dto.getOrdBaseRcvNm())
                .ordBaseAdr(dto.getOrdBaseAdr())
                .ordBaseAdrDt(dto.getOrdBaseAdrDt())
                .ordBaseMsg(dto.getOrdBaseMsg())
                .ordBaseTtAm(bigDecimal.multiply(BigDecimal.valueOf(totalQuantity)))
                .ordBaseNo(negotiation.getNgNm())
                .build();
        orderRepository.save(order);

        Map<Long, OrderItem> orderItemMap = new HashMap<>(); // 전체 수량을 QuoteDetail별로 저장(상품 수량, 이름)
        for (QuoteDetail quoteDetail : quoteDetails) {
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .ordItmQn(quoteDetail.getQuDtQn())
                    .ordItmNm(quoteDetail.getStNm())
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

        return order.getOrdBaseId();
    }

    public OrderDto mapToOrderDto(Order order) {
        String summaryName = "상품명";

        if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
            int totalItems = order.getOrderItems().size();
            String firstItemName = order.getOrderItems().get(0).getOrdItmNm();

            if (totalItems > 1) {
                summaryName = firstItemName + " 외 " + (totalItems - 1) + "건";
            } else {
                summaryName = firstItemName;
            }
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
