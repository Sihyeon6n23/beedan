package com.goodee.beedan.shipmentTests;

import com.goodee.beedan.common.constant.OrderStatus;
import com.goodee.beedan.common.constant.ShipmentStatus;
import com.goodee.beedan.dto.order.ShipmentDto;
import com.goodee.beedan.entity.Member;
import com.goodee.beedan.entity.Order;
import com.goodee.beedan.entity.Shipment;
import com.goodee.beedan.repository.order.OrderRepository;
import com.goodee.beedan.repository.order.ShipmentRepository;
import com.goodee.beedan.service.shipment.ShipmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.goodee.beedan.config.exception.EntityNotFoundException;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShipmentServiceTests {

    @Mock
    private ShipmentRepository shipmentRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private ShipmentService shipmentService;

    private Member member;
    private Order order;
    private Shipment shipment;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        member = Member.builder()
                .memId(1L)
                .build();

        order = Order.builder()
                .ordBaseId(1L)
                .member(member)
                .ordBaseStt(OrderStatus.PREPARING)
                .build();

        shipment = Shipment.builder()
                .shId(1L)
                .order(order)
                .shStt(ShipmentStatus.PREPARING)
                .shCreDt(LocalDateTime.now())
                .shCanYn(false)
                .build();

        pageable = PageRequest.of(0, 10);
    }

    @Test
    void getShipmentList_ShouldReturnShipmentPage_WhenValidRequest() {
        // Given
        Long memId = 1L;
        Long ordId = 1L;
        List<Shipment> shipments = List.of(shipment);
        Page<Shipment> shipmentPage = new PageImpl<>(shipments, pageable, 1);

        when(orderRepository.getByIdOrThrow(ordId)).thenReturn(order);
        when(shipmentRepository.findByOrder_OrdBaseIdOrderByShCreDtDesc(ordId, pageable)).thenReturn(shipmentPage);

        // When
        Page<ShipmentDto> result = shipmentService.getShipmentList(memId, ordId, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(orderRepository).getByIdOrThrow(ordId);
        verify(shipmentRepository).findByOrder_OrdBaseIdOrderByShCreDtDesc(ordId, pageable);
    }

    @Test
    void getShipmentList_ShouldThrowException_WhenMemberNotMatch() {
        // Given
        Long memId = 2L; // Different member
        Long ordId = 1L;

        when(orderRepository.getByIdOrThrow(ordId)).thenReturn(order);

        // When & Then
        assertThatThrownBy(() -> shipmentService.getShipmentList(memId, ordId, pageable))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("본인의 주문 배송 목록만 조회할 수 있습니다.");
    }

    @Test
    void getShipmentDetail_ShouldReturnShipmentDto_WhenValidRequest() {
        // Given
        Long shId = 1L;
        Long memId = 1L;
        Long ordId = 1L;

        when(shipmentRepository.getByIdOrThrow(shId)).thenReturn(shipment);

        // When
        ShipmentDto result = shipmentService.getShipmentDetail(shId, memId, ordId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getShId()).isEqualTo(shId);
        verify(shipmentRepository).getByIdOrThrow(shId);
    }

    @Test
    void getShipmentDetail_ShouldThrowException_WhenOrderNotMatch() {
        // Given
        Long shId = 1L;
        Long memId = 1L;
        Long ordId = 2L; // Different order

        when(shipmentRepository.getByIdOrThrow(shId)).thenReturn(shipment);

        // When & Then
        assertThatThrownBy(() -> shipmentService.getShipmentDetail(shId, memId, ordId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("해당 주문의 배송 내역이 아닙니다.");
    }

    @Test
    void getShipmentDetail_ShouldThrowException_WhenMemberNotMatch() {
        // Given
        Long shId = 1L;
        Long memId = 2L; // Different member
        Long ordId = 1L;

        when(shipmentRepository.getByIdOrThrow(shId)).thenReturn(shipment);

        // When & Then
        assertThatThrownBy(() -> shipmentService.getShipmentDetail(shId, memId, ordId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("본인의 주문 배송 내역만 제어할 수 있습니다.");
    }

    @Test
    void updateStatus_ShouldUpdateToShipping_WhenPreparing() {
        // Given
        Long shId = 1L;
        Long memId = 1L;
        Long ordId = 1L;

        when(shipmentRepository.getByIdOrThrow(shId)).thenReturn(shipment);

        // When
        shipmentService.updateStatus(shId, memId, ordId);

        // Then
        assertThat(shipment.getShStt()).isEqualTo(ShipmentStatus.SHIPPING);
        verify(shipmentRepository).getByIdOrThrow(shId);
    }

    @Test
    void updateStatus_ShouldThrowException_WhenInvalidState() {
        // Given
        shipment.setShStt(ShipmentStatus.DELIVERED);
        Long shId = 1L;
        Long memId = 1L;
        Long ordId = 1L;

        when(shipmentRepository.getByIdOrThrow(shId)).thenReturn(shipment);

        // When & Then
        assertThatThrownBy(() -> shipmentService.updateStatus(shId, memId, ordId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("다음 배송 단계로 자동 업데이트할 수 없는 상태입니다.");
    }

    @Test
    void updateStatusFromAdmin_ShouldUpdateStatus_WhenValid() {
        Long shId = 1L;
        Long ordId = 1L;
        ShipmentDto dto = ShipmentDto.builder()
                .shStt(ShipmentStatus.SHIPPING)
                .build();

        when(shipmentRepository.getByIdOrThrow(shId)).thenReturn(shipment);

        // When
        ShipmentDto result = shipmentService.updateStatusFromAdmin(shId, ordId, dto);

        // Then
        assertThat(result.getShStt()).isEqualTo(ShipmentStatus.SHIPPING);
        verify(shipmentRepository).getByIdOrThrow(shId);
    }

    @Test
    void updateStatusFromAdmin_ShouldThrowException_WhenShipmentNotFound() {
        // Given
        Long shId = 1L;
        Long ordId = 1L;
        ShipmentDto dto = ShipmentDto.builder().build();

        // findById 대신 서비스가 직접 호출하는 getByIdOrThrow를 Mocking 합니다.
        when(shipmentRepository.getByIdOrThrow(shId))
                .thenThrow(new EntityNotFoundException("배송 내역을 찾을 수 없습니다. ID: " + shId));

        // When & Then
        assertThatThrownBy(() -> shipmentService.updateStatusFromAdmin(shId, ordId, dto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("배송 내역을 찾을 수 없습니다. ID: 1");
    }

    @Test
    void updateStatusFromAdmin_ShouldThrowException_WhenOrderNotMatch() {
        // Given
        Long shId = 1L;
        Long ordId = 2L; // Different order
        ShipmentDto dto = ShipmentDto.builder().build();

        when(shipmentRepository.getByIdOrThrow(shId)).thenThrow(new EntityNotFoundException("배송 내역을 찾을 수 없습니다. ID: 1"));

        // When & Then
        assertThatThrownBy(() -> shipmentService.updateStatusFromAdmin(shId, ordId, dto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("배송 내역을 찾을 수 없습니다.");
    }


}
