package com.goodee.beedan.controller.admin;

import com.goodee.beedan.common.constant.OrderStatus;
import com.goodee.beedan.common.constant.ShipmentStatus;
import com.goodee.beedan.config.exception.MemberNotFoundException;
import com.goodee.beedan.config.security.MemberUserDetails;
import com.goodee.beedan.dto.admin.MemberListDto;
import com.goodee.beedan.dto.admin.MemberListResponse;
import com.goodee.beedan.dto.admin.MemberSummaryDto;
import com.goodee.beedan.dto.order.OrderDto;
import com.goodee.beedan.dto.order.ShipmentDto;
import com.goodee.beedan.dto.order.TrackingResponseDto;
import com.goodee.beedan.dto.root.scheduler.SchedulerSettingDto;
import com.goodee.beedan.entity.Shipment;
import com.goodee.beedan.repository.order.ShipmentRepository;
import com.goodee.beedan.scheduler.shipping.ShipmentScheduler;
import com.goodee.beedan.scheduler.shipping.UnipassScheduler;
import com.goodee.beedan.service.admin.AdminMemberService;
import com.goodee.beedan.service.order.OrderService;
import com.goodee.beedan.service.order.TrackingService;
import com.goodee.beedan.service.shipment.ShipmentService;
import com.goodee.beedan.service.root.SchedulerService;
import groovy.util.logging.Slf4j;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/member")
@RequiredArgsConstructor
@Slf4j
public class AdminMemberApiController {
    private static final Logger log = LoggerFactory.getLogger(AdminMemberApiController.class);
    private final AdminMemberService adminMemberService;
    private final OrderService orderService;
    private final TrackingService trackingService;
    private final ShipmentService shipmentService;

    private final ShipmentRepository shipmentRepository;
    private final UnipassScheduler unipassScheduler;
    private final ShipmentScheduler shipmentScheduler;
    private final SchedulerService schedulerService;

    @GetMapping("/list")
    public ResponseEntity<MemberListResponse> getMemberList(
            @RequestParam(required = false, defaultValue = "ALL") String status,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 6, sort = "memCreDt", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal MemberUserDetails userDetails) {
        if(userDetails == null) throw new MemberNotFoundException();

        Page<MemberListDto> memberListDtos = adminMemberService.getMembersByStatusAndKeyword(status, keyword, pageable);

        boolean isRoot = userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ROOT"));

        return ResponseEntity.ok(new MemberListResponse(memberListDtos, isRoot));
    }

    @GetMapping("/{memId}/summary")
    public ResponseEntity<MemberSummaryDto> getMemberSummary(@PathVariable Long memId, @AuthenticationPrincipal MemberUserDetails userDetails) {
        if(userDetails == null) throw new MemberNotFoundException();

        MemberSummaryDto summaryDto = adminMemberService.getMemberSummary(memId);

        return ResponseEntity.ok(summaryDto);
    }

    @GetMapping("/order/{memId}")
    public ResponseEntity<Page<OrderDto>> getMemberOrders(
            @PathVariable Long memId,
            @AuthenticationPrincipal MemberUserDetails userDetails,
            @PageableDefault(size = 6, sort = "ordBaseCreDt", direction = Sort.Direction.DESC) Pageable pageable) {
        if(userDetails == null) throw new MemberNotFoundException();

        Page<OrderDto> orderList = orderService.getListByAdmin(memId, userDetails.getMemberId(), pageable);
        return ResponseEntity.ok(orderList);
    }

    @PatchMapping("/order/{memId}/status")
    public ResponseEntity<Page<OrderDto>> updateOrderStatus(
            @PathVariable("memId") Long memId,
            @AuthenticationPrincipal MemberUserDetails userDetails,
            @RequestBody Map<String, Object> orderData, // ordBaseId, OrderStatus
            @PageableDefault(size = 10, sort = "ordBaseCreDt", direction = Sort.Direction.DESC) Pageable pageable) {
        if(userDetails == null) throw new MemberNotFoundException();

        Long ordId = Long.valueOf(orderData.get("ordBaseId").toString());
        OrderStatus ordStt = OrderStatus.valueOf(orderData.get("ordBaseStt").toString());

        orderService.updateOrderStatus(ordId, ordStt);

        Page<OrderDto> orderList = orderService.getListByAdmin(memId, userDetails.getMemberId(), pageable);
        return ResponseEntity.ok(orderList);
    }

    @GetMapping("/shipment/{memId}")
    public ResponseEntity<Page<ShipmentDto>> getMemberShipments(
            @PathVariable Long memId,
            @PageableDefault(size = 6, sort = "shCreDt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ShipmentDto> shipmentList = adminMemberService.getShipmentList(memId, pageable);

        return ResponseEntity.ok(shipmentList);
    }

    @GetMapping("/shipment/{shId}/track")
    public ResponseEntity<TrackingResponseDto> getTracking(@PathVariable("shId") Long shId, @AuthenticationPrincipal MemberUserDetails userDetails) {
        if(userDetails == null) throw new MemberNotFoundException();

        TrackingResponseDto result = trackingService.getTrackingInfo(shId);

        return ResponseEntity.ok(result);
    }

    @PostMapping("/shipment/{shId}/demo-progress")
    public ResponseEntity<String> progressDemoShipment(@PathVariable Long shId, @AuthenticationPrincipal MemberUserDetails userDetails) {
        if(userDetails == null) throw new MemberNotFoundException();

        Shipment shipment = shipmentRepository.getByIdOrThrow(shId);

        ShipmentStatus nextStatus = switch (shipment.getShStt()) {
            case PREPARING -> ShipmentStatus.SHIPPING;
            case SHIPPING -> ShipmentStatus.DELIVERING;
            case DELIVERING -> ShipmentStatus.DELIVERED;
            default -> shipment.getShStt();
        };

        ShipmentDto dto = ShipmentDto.builder().shStt(nextStatus).build();

        shipmentService.updateStatusFromAdmin(shId, shipment.getOrder().getOrdBaseId(), userDetails.getMemberId(), dto);

        return ResponseEntity.ok("배송 상태가 " + nextStatus.name() + " (으)로 변경되었습니다.");
    }

    @PostMapping("/shipment/sync-unipass")
    public ResponseEntity<String> syncUnipassManually(@AuthenticationPrincipal MemberUserDetails userDetails) {
        if(userDetails == null) throw new MemberNotFoundException();

        unipassScheduler.runUnipassTracking();

        try {
            SchedulerSettingDto setting = schedulerService.getSchedulerSetting();
            setting.setLastUnipassRunTime(LocalDateTime.now().toString());
            schedulerService.saveSchedulerSetting(setting);
        } catch (Exception e) {
            log.error("통관 수동 동기화 설정 저장 실패: {}", e.getMessage());
        }

        return ResponseEntity.ok("통관 정보 수동 동기화가 완료되었습니다.");
    }

    @PostMapping("/shipment/sync-shipment")
    public ResponseEntity<String> syncShipmentManually() {
        shipmentScheduler.syncShipmentStatus();

        SchedulerSettingDto setting = schedulerService.getSchedulerSetting();
        setting.setLastShipmentSyncRunTime(LocalDateTime.now().toString());
        schedulerService.saveSchedulerSetting(setting);

        return ResponseEntity.ok("배송 상태 수동 동기화가 완료되었습니다.");
    }
}
