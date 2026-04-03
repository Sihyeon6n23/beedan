package com.goodee.beedan.controller.order;

import com.goodee.beedan.common.constant.NotificationType;
import com.goodee.beedan.common.constant.OrderStatus;
import com.goodee.beedan.config.security.MemberUserDetails;
import com.goodee.beedan.dto.order.OrderDto;
import com.goodee.beedan.entity.Order;
import com.goodee.beedan.service.notification.NotificationService;
import com.goodee.beedan.service.order.OrderService;
import com.goodee.beedan.service.shipment.ShipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderApiController {
    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderDto> createOrder(@AuthenticationPrincipal MemberUserDetails userDetails,
                                                @RequestBody OrderDto orderDto){
        Long memId = userDetails.getMemberId();

        orderService.createOrder(memId, orderDto);

        Long createdOrdId = orderService.createOrder(memId, orderDto);

        OrderDto ordResponseDto = orderService.getOrderDetail(createdOrdId, memId);

        return ResponseEntity.ok(ordResponseDto);
    }

    @GetMapping("/list")
    public ResponseEntity<List<OrderDto>> getOrders(@AuthenticationPrincipal MemberUserDetails userDetails){
        List<OrderDto> orderList = orderService.getOrderList(userDetails.getMemberId());

        return ResponseEntity.ok(orderList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDto> getOrderDetail(@PathVariable("id") Long ordId,
                                                   @AuthenticationPrincipal MemberUserDetails userDetails) {
        OrderDto orderDetail = orderService.getOrderDetail(ordId, userDetails.getMemberId());

        return ResponseEntity.ok(orderDetail);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<OrderDto> updateOrderDetail(@PathVariable("id") Long ordId,
                                                      @AuthenticationPrincipal MemberUserDetails userDetails,
                                                      @RequestBody OrderDto orderDto) {
        Long memId = userDetails.getMemberId();
        orderService.updateOrder(ordId, memId, orderDto); // 테스트용 하드 코딩

        return ResponseEntity.ok(orderService.getOrderDetail(ordId, memId));
    }

    @PatchMapping("/{id}/admin") // 배송 상태 변경
    public ResponseEntity<OrderDto> updateOrderStatus(@PathVariable("id") Long ordId,
                                                      @RequestParam("newStatus") OrderStatus newStatus,
                                                      @AuthenticationPrincipal MemberUserDetails userDetails) {
        orderService.updateOrderStatus(ordId, newStatus);
        return ResponseEntity.ok(orderService.getOrderDetail(ordId, userDetails.getMemberId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<OrderDto> cancelOrder(@PathVariable("id") Long ordId,
                                                @AuthenticationPrincipal MemberUserDetails userDetails) {
        Long memId = userDetails.getMemberId();

        orderService.cancelOrder(ordId, memId);

        return ResponseEntity.ok(orderService.getOrderDetail(ordId, memId));
    }

}
