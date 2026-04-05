package com.goodee.beedan.controller.admin;

import com.goodee.beedan.common.constant.OrderStatus;
import com.goodee.beedan.dto.admin.MemberListDto;
import com.goodee.beedan.dto.admin.MemberSummaryDto;
import com.goodee.beedan.dto.order.OrderDto;
import com.goodee.beedan.dto.order.ShipmentDto;
import com.goodee.beedan.service.admin.AdminMemberService;
import com.goodee.beedan.service.order.OrderService;
import groovy.util.logging.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@lombok.extern.slf4j.Slf4j
@RestController
@RequestMapping("/api/admin/member")
@RequiredArgsConstructor
@Slf4j
public class AdminMemberApiController {
    private final AdminMemberService adminMemberService;
    private final OrderService orderService;

    @GetMapping("/list")
    public ResponseEntity<Page<MemberListDto>> getMemberList(
            @PageableDefault(size = 10, sort = "memCreDt", direction = Sort.Direction.DESC) Pageable pageable){
        Page<MemberListDto> memberListDtos =  adminMemberService.getAllMembers(pageable);

        return ResponseEntity.ok(memberListDtos);
    }

    @GetMapping("/{memId}/summary")
    public ResponseEntity<MemberSummaryDto> getMemberSummary(@PathVariable Long memId) {
        MemberSummaryDto summaryDto = adminMemberService.getMemberSummary(memId);

        return ResponseEntity.ok(summaryDto);
    }

    @GetMapping("/order/{memId}")
    public ResponseEntity<Page<OrderDto>> getMemberOrders(
            @PathVariable Long memId,
            @PageableDefault(size = 10, sort = "ordBaseCreDt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<OrderDto> orderList = orderService.getOrderList(memId, pageable);
        return ResponseEntity.ok(orderList);
    }

    @PatchMapping("/order/{memId}/status")
    public ResponseEntity<Page<OrderDto>> updateOrderStatus(
            @PathVariable("memId") Long memId,
            @RequestBody Map<String, Object> orderData, // ordBaseId, OrderStatus
            @PageableDefault(size = 10, sort = "ordBaseCreDt", direction = Sort.Direction.DESC) Pageable pageable) {

        Long ordId = Long.valueOf(orderData.get("ordBaseId").toString());
        OrderStatus ordStt = OrderStatus.valueOf(orderData.get("ordBaseStt").toString());

        orderService.updateOrderStatus(ordId, ordStt);

        Page<OrderDto> orderList = orderService.getOrderList(memId, pageable);
        return ResponseEntity.ok(orderList);
    }

    @GetMapping("/shipment/{memId}")
    public ResponseEntity<Page<ShipmentDto>> getMemberShipments(
            @PathVariable Long memId,
            @PageableDefault(size = 10, sort = "shCreDt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ShipmentDto> shipmentList = adminMemberService.getShipmentList(memId, pageable);

        return ResponseEntity.ok(shipmentList);
    }

}
