package com.goodee.beedan.controller.order;

import com.goodee.beedan.config.security.MemberUserDetails;
import com.goodee.beedan.dto.order.ShipmentDto;
import com.goodee.beedan.service.shipment.ShipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/api/shipments")
@RequiredArgsConstructor
public class ShipmentApiController {
    private final ShipmentService shipmentService;

    @GetMapping("/list")
    public ResponseEntity<Page<ShipmentDto>> getShipmentList(
            @AuthenticationPrincipal MemberUserDetails userDetails,
            @RequestParam(name = "ordId") Long ordId,
            @PageableDefault(size = 10, sort = "ordBaseCreDt", direction = Sort.Direction.DESC) Pageable pageable){
        Page<ShipmentDto> shipmentDtoList = shipmentService.getShipmentList(userDetails.getMemberId(), ordId, pageable);

        return ResponseEntity.ok(shipmentDtoList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShipmentDto> getShipmentDetail(
            @PathVariable(name="id") Long shId,
            @AuthenticationPrincipal MemberUserDetails userDetails,
            @RequestParam(name = "ordId") Long ordId){

        ShipmentDto shipmentDetail = shipmentService.getShipmentDetail(shId, userDetails.getMemberId(), ordId);

        return ResponseEntity.ok(shipmentDetail);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ShipmentDto> updateShipmentStatus(
            @PathVariable(name="id") Long shId,
            @AuthenticationPrincipal MemberUserDetails userDetails,
            @RequestParam(name = "ordId") Long ordId) {

        shipmentService.updateStatus(shId, userDetails.getMemberId(), ordId);
        ShipmentDto updatedShipment = shipmentService.getShipmentDetail(shId, userDetails.getMemberId(), ordId);

        return ResponseEntity.ok(updatedShipment);
    }

    @PatchMapping("/{id}/admin")
    public ResponseEntity<ShipmentDto> updateShipmentFromAdmin(
            @PathVariable(name="id") Long shId,
            @RequestParam(name="ordId") Long ordId,
            @AuthenticationPrincipal MemberUserDetails userDetails,
            @RequestBody ShipmentDto dto){

        ShipmentDto shipmentDto = shipmentService.updateStatusFromAdmin(shId, ordId, dto);

        return ResponseEntity.ok(shipmentDto);
    }

}
