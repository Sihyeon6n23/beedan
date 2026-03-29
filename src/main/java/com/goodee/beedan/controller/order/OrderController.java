package com.goodee.beedan.controller.order;

import com.goodee.beedan.config.security.MemberUserDetails;
import com.goodee.beedan.dto.order.OrderDto;
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

    @GetMapping("/list")
    public String orderList(@AuthenticationPrincipal MemberUserDetails userDetails, Model model) {

        // 1. 세션에 저장된 커스텀 유저 객체에서 memId를 바로 꺼냅니다. (DB 조회 X)
        Long memId = userDetails.getMemberId();

        // 2. 해당 ID로 주문 목록만 가져옵니다.
        List<OrderDto> orders = orderService.getOrderList(memId);
        model.addAttribute("orders", orders);

        return "member/order/order-list";
    }

    @GetMapping("/detail")
    public String orderDetail(@RequestParam("id") Long ordId, Model model, @AuthenticationPrincipal MemberUserDetails userDetails) {
        OrderDto order = orderService.getOrderDetail(ordId, userDetails.getMemberId());
        model.addAttribute("order", order);
        return "member/order/order-detail";
    }

    @PostMapping("/cancel/{id}")
    public String cancelOrder(@PathVariable("id") Long ordId, RedirectAttributes rttr, @AuthenticationPrincipal MemberUserDetails userDetails) {
        try {
            orderService.cancelOrder(ordId, userDetails.getMemberId());
            rttr.addFlashAttribute("message", "주문이 성공적으로 취소되었습니다.");
        } catch (IllegalStateException e) {
            rttr.addFlashAttribute("error", e.getMessage());
            return "redirect:/order/detail?id=" + ordId;
        }
        return "redirect:/order/list";
    }

    @PostMapping("/update/{id}")
    public String updateOrder(@PathVariable("id") Long ordId,
                              @ModelAttribute OrderDto orderDto, // Dto 필드명과 정확히 일치해야함.
                              RedirectAttributes rttr,
                              @AuthenticationPrincipal MemberUserDetails userDetails) {
        try {
            orderService.updateOrder(ordId, userDetails.getMemberId(), orderDto);
            rttr.addFlashAttribute("message", "주문 정보가 성공적으로 수정되었습니다.");
        } catch (IllegalStateException e) {
            rttr.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/order/detail?id=" + ordId;
    }

}