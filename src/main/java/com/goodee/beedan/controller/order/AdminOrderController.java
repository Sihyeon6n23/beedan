package com.goodee.beedan.controller.order;

import com.goodee.beedan.config.security.MemberUserDetails;
import com.goodee.beedan.dto.order.OrderDto;
import com.goodee.beedan.service.order.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/admin/order")
@RequiredArgsConstructor
public class AdminOrderController {
    private final OrderService orderService;

    @GetMapping("/list")
    public String getOrderList(
            @AuthenticationPrincipal MemberUserDetails userDetails,
            @PageableDefault(size = 10, sort = "ordBaseCreDt", direction = Sort.Direction.DESC) Pageable pageable,
            Model model) {

        Page<OrderDto> orders = orderService.getListByAdmin(userDetails.getMemberId(), pageable);

        model.addAttribute("orders", orders);

        return "order/order-list";
    }

    @GetMapping("/detail")
    public String getOrderDetail(@AuthenticationPrincipal MemberUserDetails userDetails,
                                 @RequestParam Long ordId, Model model){
        OrderDto order = orderService.getOrderDetail(ordId, userDetails.getMemberId());
        model.addAttribute("order", order);

        return "order/order-detail";
    }

}
