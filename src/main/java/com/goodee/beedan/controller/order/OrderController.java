package com.goodee.beedan.controller.order;

import com.goodee.beedan.common.constant.NotificationType;
import com.goodee.beedan.config.security.MemberUserDetails;
import com.goodee.beedan.dto.order.OrderDto;
import com.goodee.beedan.service.notification.NotificationService;
import com.goodee.beedan.service.order.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    private final NotificationService notificationService;

    @GetMapping("/list")
    public String orderList(@AuthenticationPrincipal MemberUserDetails userDetails, Model model) {
        Long memId = userDetails.getMemberId();

        List<OrderDto> orders = orderService.getOrderList(memId);
        model.addAttribute("orders", orders);

        return "order/order-list";
    }

    @GetMapping("/detail")
    public String orderDetail(@RequestParam("id") Long ordId, Model model, @AuthenticationPrincipal MemberUserDetails userDetails) {
        OrderDto order = orderService.getOrderDetail(ordId, userDetails.getMemberId());
        model.addAttribute("order", order);
        return "order/order-detail";
    }

}