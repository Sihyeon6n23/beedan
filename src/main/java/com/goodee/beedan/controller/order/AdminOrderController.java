package com.goodee.beedan.controller.order;

import com.goodee.beedan.common.constant.OrderStatus;
import com.goodee.beedan.config.security.MemberUserDetails;
import com.goodee.beedan.dto.order.OrderDto;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/order")
@RequiredArgsConstructor
@Slf4j
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
        OrderDto order = orderService.getOrderDetail(ordId, userDetails);

        model.addAttribute("order", order);

        return "order/order-detail";
    }

    @PostMapping("/update-status")
    public String updateOrderStatus(@RequestParam("ordId") Long ordId,
                                    @RequestParam("status") OrderStatus status,
                                    RedirectAttributes redirectAttributes) {

        try {
            orderService.updateOrderStatus(ordId, status);
            redirectAttributes.addFlashAttribute("message", "주문 상태가 [" + status.name() + "](으)로 변경되었습니다.");
        } catch (Exception e) {
            log.error("상태 변경 중 오류 발생", e);
            redirectAttributes.addFlashAttribute("errorMessage", "상태 변경 실패: " + e.getMessage());
        }

        return "redirect:/order/detail?id=" + ordId;
    }

}
