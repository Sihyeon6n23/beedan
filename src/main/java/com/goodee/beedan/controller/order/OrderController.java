package com.goodee.beedan.controller.order;

import com.goodee.beedan.common.constant.NotificationType;
import com.goodee.beedan.config.security.MemberUserDetails;
import com.goodee.beedan.dto.order.OrderDto;
import com.goodee.beedan.service.notification.NotificationService;
import com.goodee.beedan.service.order.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/order")
@RequiredArgsConstructor
@Slf4j
public class OrderController {
    private final OrderService orderService;
    private final NotificationService notificationService;

    @GetMapping("/list")
    public String orderList(@AuthenticationPrincipal MemberUserDetails userDetails,
                            @PageableDefault(size = 10, sort = "ordBaseCreDt", direction = Sort.Direction.DESC) Pageable pageable,
                            Model model) {
        Long memId = userDetails.getMemberId();

        Page<OrderDto> orders = orderService.getOrderList(memId, pageable);
        model.addAttribute("orders", orders);

        return "order/order-list";
    }

    @GetMapping("/detail")
    public String orderDetail(@RequestParam("id") Long ordId,
                              @AuthenticationPrincipal MemberUserDetails userDetails,
                              Model model) {
        OrderDto order = orderService.getOrderDetail(ordId, userDetails.getMemberId());
        model.addAttribute("order", order);

        return "order/order-detail";
    }

    @PostMapping("/cancel")
    public String cancelOrder(@RequestParam("ordId") Long ordId,
                              @AuthenticationPrincipal MemberUserDetails userDetails,
                              RedirectAttributes redirectAttributes) {

        try {
            orderService.cancelOrder(ordId, userDetails.getMemberId());
            redirectAttributes.addFlashAttribute("message", "주문이 정상적으로 취소되었습니다.");

            notificationService.createNotification(userDetails.getMemberId(), NotificationType.ORDER_CANCEL, ordId);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/order/detail?id=" + ordId;
    }

}