package com.goodee.beedan.controller.admin;

import com.goodee.beedan.common.constant.OrderStatus;
import com.goodee.beedan.common.constant.ShipmentStatus;
import com.goodee.beedan.config.security.MemberUserDetails;
import com.goodee.beedan.dto.admin.MemberListDto;
import com.goodee.beedan.dto.admin.MemberListResponse;
import com.goodee.beedan.dto.admin.MemberSummaryDto;
import com.goodee.beedan.dto.order.OrderDto;
import com.goodee.beedan.dto.order.ShipmentDto;
import com.goodee.beedan.dto.order.TrackingResponseDto;
import com.goodee.beedan.entity.Shipment;
import com.goodee.beedan.repository.order.ShipmentRepository;
import com.goodee.beedan.scheduler.Order.UnipassScheduler;
import com.goodee.beedan.service.admin.AdminMemberService;
import com.goodee.beedan.service.order.OrderService;
import com.goodee.beedan.service.order.TrackingService;
import com.goodee.beedan.service.shipment.ShipmentService;
import groovy.util.logging.Slf4j;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
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
    private final TrackingService trackingService;
    private final ShipmentService shipmentService;

    private final ShipmentRepository shipmentRepository;
    private final UnipassScheduler unipassScheduler;

    @GetMapping("/list")
    public ResponseEntity<MemberListResponse> getMemberList(
            @RequestParam(required = false, defaultValue = "ALL") String status,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10, sort = "memCreDt", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal MemberUserDetails userDetails) {
        Page<MemberListDto> memberListDtos = adminMemberService.getMembersByStatusAndKeyword(status, keyword, pageable);

        boolean isRoot = userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ROOT"));

        return ResponseEntity.ok(new MemberListResponse(memberListDtos, isRoot));
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

    @GetMapping("/shipment/{shId}/track")
    public ResponseEntity<TrackingResponseDto> getTracking(@PathVariable("shId") Long shId) {
        TrackingResponseDto result = trackingService.getTrackingInfo(shId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/shipment/{shId}/demo-progress")
    public ResponseEntity<String> progressDemoShipment(@PathVariable Long shId) {
        Shipment shipment = shipmentRepository.findById(shId).orElseThrow(() -> new IllegalArgumentException("배송 내역을 찾을 수 없습니다."));

        ShipmentStatus nextStatus = switch (shipment.getShStt()) {
            case PREPARING -> ShipmentStatus.SHIPPING;
            case SHIPPING -> ShipmentStatus.DELIVERING;
            case DELIVERING -> ShipmentStatus.DELIVERED;
            default -> shipment.getShStt();
        };

        ShipmentDto dto = ShipmentDto.builder().shStt(nextStatus).build();

        shipmentService.updateStatusFromAdmin(shId, shipment.getOrder().getOrdBaseId(), dto);

        return ResponseEntity.ok("배송 상태가 " + nextStatus.name() + " (으)로 변경되었습니다.");
    }

    @PostMapping("/shipment/sync-unipass")
    public ResponseEntity<String> syncUnipassManually() {
        unipassScheduler.runUnipassTracking();
        return ResponseEntity.ok("통관 정보 수동 동기화가 완료되었습니다.");
    }

}
