package com.goodee.beedan.controller.member.order;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {
    @GetMapping("/list")
    public String getOrderList(){
        return "member/order/order-list";
    }
}
