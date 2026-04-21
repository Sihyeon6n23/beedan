package com.goodee.beedan.controller.order;

import com.goodee.beedan.service.order.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/order")
@RequiredArgsConstructor
@Slf4j
public class OrderController {
    private final OrderService orderService;

    @GetMapping("/list")
    public String orderList() {
        return "order/order-list";
    }

    @GetMapping("/detail")
    public String orderDetail() {
        return "order/order-detail";
    }

}