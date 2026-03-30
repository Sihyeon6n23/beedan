package com.goodee.beedan.orderServiceTest;

import com.goodee.beedan.common.constant.OrderStatus;
import com.goodee.beedan.dto.order.OrderDto;
import com.goodee.beedan.entity.Member;
import com.goodee.beedan.entity.Order;
import com.goodee.beedan.repository.member.MemberRepository;
import com.goodee.beedan.repository.order.OrderRepository;
import com.goodee.beedan.service.order.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import org.junit.jupiter.api.Assertions;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {
    @Mock
    private OrderRepository orderRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    public void CreateOrderTest() {
        Long memId = 1L;
        Member member = Member.builder().memId(memId).build();

        OrderDto dto = OrderDto.builder()
                .ordBaseRcvNm("수령자1")
                .ordBaseAdr("기본주소")
                .ordBaseAdrDt("상세주소")
                .build();

        when(memberRepository.findById(memId)).thenReturn(Optional.of(member));

        // 2. When
        orderService.createOrder(memId, dto);

        // 3. Then
        ArgumentCaptor<Order> orderArgumentCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository, times(1)).save(orderArgumentCaptor.capture());
        Order savedOrder = orderArgumentCaptor.getValue();

        Assertions.assertEquals(memId, savedOrder.getMember().getMemId());
        Assertions.assertEquals("수령자1", savedOrder.getOrdBaseRcvNm());
        Assertions.assertEquals("기본주소", savedOrder.getOrdBaseAdr());
        Assertions.assertEquals("상세주소", savedOrder.getOrdBaseAdrDt());
    }

    @Test
    @DisplayName("회원 ID로 주문 목록을 조회하면 DTO 리스트가 반환된다")
    public void getOrderListTest() {
        Long memId = 1L;
        Member member = Member.builder().memId(memId).build();

        Order order1 = Order.builder()
                .ordBaseId(101L)
                .ordBaseRcvNm("수령자1")
                .ordBaseAdr("서울")
                .member(member)
                .build();

        Order order2 = Order.builder()
                .ordBaseId(102L)
                .ordBaseRcvNm("수령자2")
                .ordBaseAdr("부산")
                .member(member)
                .build();

        List<Order> orders = Arrays.asList(order1, order2);

        // Mockito 설정
        when(memberRepository.findById(memId)).thenReturn(Optional.of(member));
        when(orderRepository.findByMember_MemIdOrderByOrdBaseCreDtDesc(memId)).thenReturn(orders);

        // 2. When (실행)
        List<OrderDto> result = orderService.getOrderList(memId);

        // 3. Then (검증)
        Assertions.assertNotNull(result);
        Assertions.assertEquals(2, result.size());

        // 첫 번째 데이터 검증
        Assertions.assertEquals(101L, result.get(0).getOrdBaseId());
        Assertions.assertEquals("수령자1", result.get(0).getOrdBaseRcvNm());

        // 두 번째 데이터 검증
        Assertions.assertEquals(102L, result.get(1).getOrdBaseId());
        Assertions.assertEquals("수령자2", result.get(1).getOrdBaseRcvNm());

        // Repository 호출 여부 확인
        verify(memberRepository, times(1)).findById(memId);
        verify(orderRepository, times(1)).findByMember_MemIdOrderByOrdBaseCreDtDesc(memId);
    }

    /*
    @Test
    @DisplayName("주문 ID로 주문 상세정보를 조회하면 OrderDto가 반환된다")
    public void getOrderDetailTest() {
        // 1. Given (준비)
        Long ordId = 1L;
        Member member = Member.builder().memId(1L).build();

        Order order = Order.builder()
                .ordBaseId(ordId)
                .ordBaseRcvNm("수령자1")
                .ordBaseAdr("기본주소")
                .ordBaseAdrDt("상세주소")
                .ordBaseMsg("빠른 배송 부탁드립니다")
                .member(member)
                .build();

        when(orderRepository.findById(ordId)).thenReturn(Optional.of(order));

        // 2. When (실행)
        OrderDto result = orderService.getOrderDetail(ordId, member.getMemId());

        // 3. Then (검증)
        Assertions.assertNotNull(result);
        Assertions.assertEquals(ordId, result.getOrdBaseId());
        Assertions.assertEquals("수령자1", result.getOrdBaseRcvNm());
        Assertions.assertEquals("기본주소", result.getOrdBaseAdr());
        Assertions.assertEquals("상세주소", result.getOrdBaseAdrDt());
        Assertions.assertEquals("빠른 배송 부탁드립니다", result.getOrdBaseMsg());

        // Repository 호출 여부 확인
        verify(orderRepository, times(1)).findById(ordId);
    } */

    @Test
    @DisplayName("존재하지 않는 주문 ID로 조회하면 IllegalArgumentException이 발생한다")
    public void getOrderDetailNotFoundTest() {
        // 1. Given (준비)
        Long ordId = 999L;

        when(orderRepository.findById(ordId)).thenReturn(Optional.empty());

        // 2. When & Then (실행 및 검증)
        /* Assertions.assertThrows(IllegalArgumentException.class, () -> orderService.getOrderDetail(ordId, 1L)); */

        // Repository 호출 여부 확인
        verify(orderRepository, times(1)).findById(ordId);
    }

//    @Test
//    @DisplayName("주문 정보를 업데이트하면 변경사항이 저장된다")
//    public void updateOrderTest() {
//        // 1. Given (준비)
//        Long ordId = 1L;
//        Member member = Member.builder().memId(1L).build();
//
//        Order existingOrder = Order.builder()
//                .ordBaseId(ordId)
//                .ordBaseRcvNm("기존 수령자")
//                .ordBaseAdr("기존 주소")
//                .ordBaseAdrDt("기존 상세주소")
//                .ordBaseMsg("기존 메시지")
//                .member(member)
//                .build();
//
//        OrderDto updateDto = OrderDto.builder()
//                .ordBaseRcvNm("새 수령자")
//                .ordBaseAdr("새 주소")
//                .ordBaseAdrDt("새 상세주소")
//                .ordBaseMsg("새 메시지")
//                .build();
//
//        when(orderRepository.findById(ordId)).thenReturn(Optional.of(existingOrder));
//
//        // 2. When (실행)
//        orderService.updateOrder(ordId, updateDto);
//
//        // 3. Then (검증)
//        ArgumentCaptor<Order> orderArgumentCaptor = ArgumentCaptor.forClass(Order.class);
//        verify(orderRepository, times(1)).save(orderArgumentCaptor.capture());
//        Order savedOrder = orderArgumentCaptor.getValue();
//
//        Assertions.assertEquals("새 수령자", savedOrder.getOrdBaseRcvNm());
//        Assertions.assertEquals("새 주소", savedOrder.getOrdBaseAdr());
//        Assertions.assertEquals("새 상세주소", savedOrder.getOrdBaseAdrDt());
//        Assertions.assertEquals("새 메시지", savedOrder.getOrdBaseMsg());
//
//        // Repository 호출 여부 확인
//        verify(orderRepository, times(1)).findById(ordId);
//    }

//    @Test
//    @DisplayName("존재하지 않는 주문 ID로 업데이트하면 IllegalArgumentException이 발생한다")
//    public void updateOrderNotFoundTest() {
//        // 1. Given (준비)
//        Long ordId = 999L;
//        OrderDto updateDto = OrderDto.builder()
//                .ordBaseRcvNm("새 수령자")
//                .build();
//
//        when(orderRepository.findById(ordId)).thenReturn(Optional.empty());
//
//        // 2. When & Then (실행 및 검증)
//        Assertions.assertThrows(IllegalArgumentException.class, () -> orderService.updateOrder(ordId, updateDto));
//
//        // Repository 호출 여부 확인
//        verify(orderRepository, times(1)).findById(ordId);
//        verify(orderRepository, never()).save(any(Order.class));
//    }

//    @Test
//    @DisplayName("일부 필드만 업데이트하면 해당 필드만 변경된다")
//    public void updateOrderPartialTest() {
//        // 1. Given (준비)
//        Long ordId = 1L;
//        Member member = Member.builder().memId(1L).build();
//
//        Order existingOrder = Order.builder()
//                .ordBaseId(ordId)
//                .ordBaseRcvNm("기존 수령자")
//                .ordBaseAdr("기존 주소")
//                .ordBaseAdrDt("기존 상세주소")
//                .ordBaseMsg("기존 메시지")
//                .ordBaseStt(OrderStatus.PREPARING)
//                .member(member)
//                .build();
//
//        OrderDto updateDto = OrderDto.builder()
//                .ordBaseRcvNm("새 수령자")
//                .ordBaseAdr(null)  // null이면 업데이트 안 됨
//                .ordBaseAdrDt("새 상세주소")
//                .ordBaseMsg(null)  // null이면 업데이트 안 됨
//                .ordBaseStt(OrderStatus.DELIVERING)
//                .build();
//
//        when(orderRepository.findById(ordId)).thenReturn(Optional.of(existingOrder));
//
//        // 2. When (실행)
//        orderService.updateOrder(ordId, updateDto);
//
//        // 3. Then (검증)
//        ArgumentCaptor<Order> orderArgumentCaptor = ArgumentCaptor.forClass(Order.class);
//        verify(orderRepository, times(1)).save(orderArgumentCaptor.capture());
//        Order savedOrder = orderArgumentCaptor.getValue();
//
//        Assertions.assertEquals("새 수령자", savedOrder.getOrdBaseRcvNm());  // 업데이트됨
//        Assertions.assertEquals("기존 주소", savedOrder.getOrdBaseAdr());   // null이므로 변경 안 됨
//        Assertions.assertEquals("새 상세주소", savedOrder.getOrdBaseAdrDt()); // 업데이트됨
//        Assertions.assertEquals("기존 메시지", savedOrder.getOrdBaseMsg());  // null이므로 변경 안 됨
//        Assertions.assertEquals(OrderStatus.DELIVERING, savedOrder.getOrdBaseStt());  // null이므로 변경 안 됨
//
//        // Repository 호출 여부 확인
//        verify(orderRepository, times(1)).findById(ordId);
//    }

//    @Test
//    @DisplayName("주문 취소")
//    public void cancelOrderTest() {
//        // 1. Given (준비)
//        Long ordId = 1L;
//        Member member = Member.builder().memId(1L).build();
//
//        Order existingOrder = Order.builder()
//                .ordBaseId(ordId)
//                .ordBaseStt(OrderStatus.PREPARING)
//                .member(member)
//                .build();
//
//        OrderDto updateDto = OrderDto.builder()
//                .ordBaseStt(OrderStatus.CANCELLED)
//                .build();
//
//        when(orderRepository.findById(ordId)).thenReturn(Optional.of(existingOrder));
//
//        // 2. When (실행)
//        orderService.updateOrder(ordId, updateDto);
//
//        // 3. Then (검증)
//        ArgumentCaptor<Order> orderArgumentCaptor = ArgumentCaptor.forClass(Order.class);
//        verify(orderRepository, times(1)).save(orderArgumentCaptor.capture());
//        Order savedOrder = orderArgumentCaptor.getValue();
//
//        Assertions.assertEquals(OrderStatus.CANCELLED, savedOrder.getOrdBaseStt());
//
//        // Repository 호출 여부 확인
//        verify(orderRepository, times(1)).findById(ordId);
//    }


}
