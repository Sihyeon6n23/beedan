package com.goodee.beedan.controller.order;

import com.goodee.beedan.common.constant.OrderStatus;
import com.goodee.beedan.config.security.MemberUserDetails;
import com.goodee.beedan.dto.order.OrderDto;
import com.goodee.beedan.entity.Order;
import com.goodee.beedan.service.order.OrderService;
import com.goodee.beedan.service.shipment.ShipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/test/orders")
@RequiredArgsConstructor
public class OrderApiController {
    private final OrderService orderService;
    private final ShipmentService shipmentService;

    @PostMapping
    public ResponseEntity<OrderDto> createOrder(){
        OrderDto orderDto = OrderDto.builder()
                .ordBaseMsg("테스트용")
                .ordBaseAdr("서울시 강남구 테헤란로 123")
                .ordBaseAdrDt("101동 202호")
                .ordBaseRcvNm("홍길동")
                .ordBaseNo("0000222224444")
                .build();
        orderService.createOrder(1L, orderDto);

        OrderDto dto = orderService.getOrderDetail(6L,1L);

        return ResponseEntity.ok(dto);
    }

    @GetMapping
    public ResponseEntity<List<OrderDto>> getOrders(){
        List<OrderDto> orderList = orderService.getOrderList(1L); // 테스트용 하드 코딩

        return ResponseEntity.ok(orderList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDto> getOrderDetail(@PathVariable("id") Long ordId) {
        OrderDto orderDetail = orderService.getOrderDetail(ordId, 1L); // 테스트용 하드 코딩

        return ResponseEntity.ok(orderDetail);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<OrderDto> updateOrderDetail(@PathVariable("id") Long ordId) {
        OrderDto dto = OrderDto.builder()
                .ordBaseAdr("경기도 안양시")
                .ordBaseAdrDt("201동 505호")
                .ordBaseMsg("배송전 연락 바랍니다.")
                .build();

        orderService.updateOrder(ordId, 1L, dto); // 테스트용 하드 코딩

        return ResponseEntity.ok(orderService.getOrderDetail(ordId, 1L));
    }

    @PatchMapping("/{id}/admin")
    public ResponseEntity<OrderDto> updateOrderStatus(@PathVariable("id") Long ordId,
                                                      @RequestParam("status") OrderStatus newStatus) {
        orderService.updateOrderStatus(ordId, newStatus);

        return ResponseEntity.ok(orderService.getOrderDetail(ordId, 1L));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<OrderDto> cancelOrder(@PathVariable("id") Long ordId) {
        /*if(userDetails.getAuthorities().equals("ROLE_ADMIN")) {
            orderService.updateOrderStatus(ordId, OrderStatus.CANCELLED);
            return ResponseEntity.ok(orderService.getOrderDetail(ordId, userDetails.getMemId()));
        }*/
        orderService.cancelOrder(ordId, 1L);

        return ResponseEntity.ok(orderService.getOrderDetail(ordId, 1L));
    }

}
