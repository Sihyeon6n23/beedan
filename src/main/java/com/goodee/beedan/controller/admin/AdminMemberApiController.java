package com.goodee.beedan.controller.admin;

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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @GetMapping("/{memberId}/summary")
    public ResponseEntity<MemberSummaryDto> getMemberSummary(@PathVariable Long memberId) {
        MemberSummaryDto summaryDto = adminMemberService.getMemberSummary(memberId);

        return ResponseEntity.ok(summaryDto);
    }

    @GetMapping("/order/{memberId}")
    public ResponseEntity<Page<OrderDto>> getMemberOrders(
            @PathVariable Long memberId,
            @PageableDefault(size = 10, sort = "ordBaseCreDt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<OrderDto> orderList = orderService.getOrderList(memberId, pageable);
        return ResponseEntity.ok(orderList);
    }

    @GetMapping("/shipment/{memberId}")
    public ResponseEntity<Page<ShipmentDto>> getMemberShipments(
            @PathVariable Long memberId,
            @PageableDefault(size = 10, sort = "shCreDt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ShipmentDto> shipmentList = adminMemberService.getShipmentList(memberId, pageable);

        return ResponseEntity.ok(shipmentList);
    }

}
