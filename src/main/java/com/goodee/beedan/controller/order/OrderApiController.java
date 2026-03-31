package com.goodee.beedan.controller.order;

import com.goodee.beedan.dto.order.OrderDto;
import com.goodee.beedan.entity.Order;
import com.goodee.beedan.service.order.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test/orders")
@RequiredArgsConstructor
public class OrderApiController {
    private final OrderService orderService;

    @PostMapping("/create")
    public ResponseEntity<OrderDto> createOrder(){
        OrderDto orderDto = OrderDto.builder()
                .ordBaseMsg("테스트용")
                .ordBaseAdr("서울시 강남구 테헤란로 123")
                .ordBaseAdrDt("101동 202호")
                .ordBaseRcvNm("홍길동")
                .ordBaseNo("0000222224444")
                .build();
        orderService.createOrder(1L, orderDto);

        OrderDto dto = orderService.getOrderDetail(6L, 1L);

        return ResponseEntity.ok(dto);
    }


}
