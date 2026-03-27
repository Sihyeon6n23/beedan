package com.goodee.beedan.controller.order;

import com.goodee.beedan.config.security.MemberUserDetails;
import com.goodee.beedan.dto.order.OrderDto;
import com.goodee.beedan.service.order.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    @GetMapping("/list")
    public String getOrderList(@AuthenticationPrincipal MemberUserDetails userDetails, Model model){
        Long memId = userDetails.getMemberId();
        List<OrderDto> orderDtoList = orderService.getOrderList(memId);

        model.addAttribute("orderDtoList", orderDtoList);

        return "member/order/order-list";
    }
}
