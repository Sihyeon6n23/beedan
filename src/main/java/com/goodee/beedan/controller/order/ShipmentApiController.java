package com.goodee.beedan.controller.order;

import com.goodee.beedan.config.security.MemberUserDetails;
import com.goodee.beedan.dto.order.TrackingResponseDto;
import com.goodee.beedan.entity.Shipment;
import com.goodee.beedan.repository.order.ShipmentRepository;
import com.goodee.beedan.service.order.TrackingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/api/shipments")
@RequiredArgsConstructor
public class ShipmentApiController {
    private final TrackingService trackingService;
    private final ShipmentRepository shipmentRepository;

    @GetMapping("/{shId}/track")
    public ResponseEntity<TrackingResponseDto> getTrackingInfo(@PathVariable("shId") Long shId,
                                                               @AuthenticationPrincipal MemberUserDetails userDetails) {
        Shipment shipment = shipmentRepository.getByIdOrThrow(shId);

        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !shipment.getOrder().getMember().getMemId().equals(userDetails.getMemberId())) {
            throw new IllegalArgumentException();
        }

        TrackingResponseDto result = trackingService.getTrackingInfo(shId);
        return ResponseEntity.ok(result);
    }

}
