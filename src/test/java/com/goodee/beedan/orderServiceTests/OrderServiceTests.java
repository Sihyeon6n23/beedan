package com.goodee.beedan.orderServiceTests;

import com.goodee.beedan.common.constant.MemberAuthority;
import com.goodee.beedan.common.constant.OrderStatus;
import com.goodee.beedan.common.constant.ShipmentStatus;
import com.goodee.beedan.config.security.MemberUserDetails;
import com.goodee.beedan.dto.order.OrderDto;
import com.goodee.beedan.entity.*;
import com.goodee.beedan.repository.member.MemberRepository;
import com.goodee.beedan.repository.order.*;
import com.goodee.beedan.repository.payment.PaymentRepository;
import com.goodee.beedan.repository.quote.*;
import com.goodee.beedan.repository.stock.StockRepository;
import com.goodee.beedan.service.order.OrderService;
import com.goodee.beedan.service.order.ThumbnailRedisService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTests {

    @InjectMocks
    private OrderService orderService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private QuoteDetailRepository quoteDetailRepository;

    @Mock
    private NegotiationRepository negotiationRepository;

    @Mock
    private ShipmentRepository shipmentRepository;

    @Mock
    private ShipmentItemRepository shipmentItemRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private QuoteBaseRepository quoteBaseRepository;

    @Mock
    private StockRepository stockRepository;

    @Mock
    private ThumbnailRedisService thumbnailRedisService;

    private Member member;
    private Order order;
    private MemberUserDetails userDetails;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        member = Member.builder()
                .memId(1L)
                .memNm("테스트 사용자")
                .memLgnId("testuser")
                .memEml("test@example.com")
                .memAut(MemberAuthority.USER)
                .build();

        order = Order.builder()
                .ordBaseId(1L)
                .member(member)
                .ordBaseNo("ORD-001")
                .ordBaseRcvNm("수령인")
                .ordBaseAdr("서울시 강남구")
                .ordBaseAdrDt("상세주소")
                .ordBaseMsg("배송메시지")
                .ordBaseStt(OrderStatus.PREPARING)
                .ordBaseTtAm(BigDecimal.valueOf(100000))
                .ordBaseCreDt(LocalDateTime.now())
                .build();

        userDetails = new MemberUserDetails(member, null);
        userDetails.setAuthorities(List.of(new SimpleGrantedAuthority("ROLE_USER")));
        pageable = PageRequest.of(0, 10);
    }

    @Test
    @DisplayName("주문 목록 조회 성공 - 회원의 주문 목록 반환")
    void getOrderList_Success() {
        // given
        Long memId = 1L;
        List<Order> orders = List.of(order);
        Page<Order> orderPage = new PageImpl<>(orders, pageable, 1);

        given(memberRepository.existsById(memId)).willReturn(true);
        given(orderRepository.findByMember_MemIdOrderByOrdBaseCreDtDesc(memId, pageable)).willReturn(orderPage);

        // when
        Page<OrderDto> result = orderService.getOrderList(memId, "PREPARING", pageable);

        // then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("ORD-001", result.getContent().getFirst().getOrdBaseNo());
        verify(orderRepository, times(1)).findByMember_MemIdOrderByOrdBaseCreDtDesc(memId, pageable);
    }

    @Test
    @DisplayName("주문 목록 조회 실패 - 존재하지 않는 회원")
    void getOrderList_Fail_MemberNotFound() {
        // given
        Long memId = 999L;

        given(memberRepository.existsById(memId)).willReturn(false);

        // when
        Page<OrderDto> result = orderService.getOrderList(memId, "PREPARING", pageable);

        // then
        assertNull(result);
        verify(orderRepository, never()).findByMember_MemIdOrderByOrdBaseCreDtDesc(anyLong(), any(Pageable.class));
    }

    @Test
    @DisplayName("주문 상세 조회 실패 - 권한 없음")
    void getOrderDetail_Fail_NoPermission() {
        // given
        Long ordId = 1L;

        given(orderRepository.getByIdOrThrow(ordId)).willReturn(order);

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> orderService.getOrderDetail(ordId, 2L));
        assertEquals("해당 주문에 대한 조회 권한이 없습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("주문 정보 수정 성공")
    void updateOrder_Success() {
        // given
        Long ordId = 1L;
        Long memId = 1L;
        OrderDto updateDto = OrderDto.builder()
                .ordBaseAdr("새 주소")
                .ordBaseAdrDt("새 상세주소")
                .ordBaseRcvNm("새 수령인")
                .ordBaseMsg("새 메시지")
                .build();

        given(orderRepository.getByIdOrThrow(ordId)).willReturn(order);

        // when
        orderService.updateOrder(ordId, memId, updateDto);

        // then
        assertEquals("새 주소", order.getOrdBaseAdr());
        assertEquals("새 상세주소", order.getOrdBaseAdrDt());
        assertEquals("새 수령인", order.getOrdBaseRcvNm());
        assertEquals("새 메시지", order.getOrdBaseMsg());
    }

    @Test
    @DisplayName("주문 정보 수정 실패 - 권한 없음")
    void updateOrder_Fail_NoPermission() {
        // given
        Long ordId = 1L;
        Long memId = 999L; // 다른 회원
        OrderDto updateDto = OrderDto.builder().ordBaseAdr("새 주소").build();

        given(orderRepository.getByIdOrThrow(ordId)).willReturn(order);

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> orderService.updateOrder(ordId, memId, updateDto));
        assertEquals("본인의 주문만 수정할 수 있습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("주문 정보 수정 실패 - 배송 준비 중이 아님")
    void updateOrder_Fail_InvalidStatus() {
        // given
        Long ordId = 1L;
        Long memId = 1L;
        order.setOrdBaseStt(OrderStatus.DELIVERING); // 배송 중으로 변경
        OrderDto updateDto = OrderDto.builder().ordBaseAdr("새 주소").build();

        given(orderRepository.getByIdOrThrow(ordId)).willReturn(order);

        // when & then
        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> orderService.updateOrder(ordId, memId, updateDto));
        assertEquals("배송 준비 중일 때만 정보를 수정할 수 있습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("주문 상태 변경 성공")
    void updateOrderStatus_Success() {
        // given
        Long ordId = 1L;
        OrderStatus newStatus = OrderStatus.DELIVERING;

        given(orderRepository.getByIdOrThrow(ordId)).willReturn(order);

        // when
        orderService.updateOrderStatus(ordId, newStatus);

        // then
        assertEquals(OrderStatus.DELIVERING, order.getOrdBaseStt());
    }

    @Test
    @DisplayName("주문 상태 변경 실패 - 취소된 주문")
    void updateOrderStatus_Fail_CanceledOrder() {
        // given
        Long ordId = 1L;
        order.setOrdBaseStt(OrderStatus.CANCELED);
        OrderStatus newStatus = OrderStatus.DELIVERING;

        given(orderRepository.getByIdOrThrow(ordId)).willReturn(order);

        // when & then
        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> orderService.updateOrderStatus(ordId, newStatus));
        assertEquals("취소된 주문의 상태는 변경할 수 없습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("모든 배송 완료 시 주문 상태 변경 성공")
    void updateOrderIfAllShipmentsComplete_Success() {
        // given
        Long ordId = 1L;
        Shipment shipment1 = Shipment.builder().shStt(ShipmentStatus.DELIVERED).build();
        Shipment shipment2 = Shipment.builder().shStt(ShipmentStatus.DELIVERED).build();
        List<Shipment> shipments = List.of(shipment1, shipment2);

        given(orderRepository.getByIdOrThrow(ordId)).willReturn(order);
        given(shipmentRepository.findByOrder(order)).willReturn(shipments);

        // when
        orderService.updateOrderIfAllShipmentsComplete(ordId);

        // then
        assertEquals(OrderStatus.DELIVERED, order.getOrdBaseStt());
    }

    @Test
    @DisplayName("주문 취소 성공")
    void cancelOrder_Success() {
        // given
        Long ordId = 1L;
        Long memId = 1L;
        Shipment shipment = Shipment.builder().shCanYn(false).build();
        order.setShipments(List.of(shipment));

        given(memberRepository.existsById(memId)).willReturn(true);
        given(orderRepository.findById(ordId)).willReturn(Optional.of(order));

        // when
        orderService.cancelOrder(ordId, memId);

        // then
        assertEquals(OrderStatus.CANCELED, order.getOrdBaseStt());
        assertTrue(shipment.getShCanYn());
    }

    @Test
    @DisplayName("주문 취소 실패 - 이미 배송 중")
    void cancelOrder_Fail_AlreadyDelivering() {
        // given
        Long ordId = 1L;
        Long memId = 1L;
        order.setOrdBaseStt(OrderStatus.DELIVERING);

        given(memberRepository.existsById(memId)).willReturn(true);
        given(orderRepository.findById(ordId)).willReturn(Optional.of(order));

        // when & then
        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> orderService.cancelOrder(ordId, memId));
        assertEquals("이미 배송이 시작되어 취소할 수 없습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("관리자용 주문 목록 조회 성공")
    void getListByAdmin_Success() {
        // given
        Long adminMemId = 2L;
        Member admin = Member.builder().memId(adminMemId).memAut(MemberAuthority.ADMIN).build();
        List<Order> orders = List.of(order);
        Page<Order> orderPage = new PageImpl<>(orders, pageable, 1);

        given(memberRepository.findById(adminMemId)).willReturn(Optional.of(admin));
        given(orderRepository.findAll(pageable)).willReturn(orderPage);

        // when
        Page<OrderDto> result = orderService.getListByAdmin(1L, adminMemId, pageable);

        // then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(orderRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("관리자용 주문 목록 조회 실패 - 관리자 권한 없음")
    void getListByAdmin_Fail_NoAdminPermission() {
        // given
        Long userMemId = 1L;

        given(memberRepository.findById(userMemId)).willReturn(Optional.of(member));

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> orderService.getListByAdmin(userMemId, 1L, pageable));
        assertEquals("관리자만 주문 목록을 조회할 수 있습니다.", exception.getMessage());
    }
}
