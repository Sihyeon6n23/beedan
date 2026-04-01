package com.goodee.beedan.controller.order;

import com.goodee.beedan.config.security.MemberUserDetails;
import com.goodee.beedan.dto.order.ShipmentDto;
import com.goodee.beedan.service.shipment.ShipmentService;
import lombok.RequiredArgsConstructor;
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

    @GetMapping("/list/{id}")
    public ResponseEntity<List<ShipmentDto>> getShipment(
            @PathVariable(name="id") Long shId,
            @AuthenticationPrincipal MemberUserDetails userDetails,
            @RequestParam(name = "ordId") Long ordId){
        List<ShipmentDto> shipmentDtoList = shipmentService.getShipmentList(shId, ordId);

        return ResponseEntity.ok(shipmentDtoList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShipmentDto> getShipmentDetail(
            @PathVariable(name="id") Long shId,
            @AuthenticationPrincipal MemberUserDetails userDetails,
            @RequestParam(name = "ordId") Long ordId){
        shipmentService.updateStatus(shId, userDetails.getMemberId(), ordId);

        ShipmentDto updatedShipment = shipmentService.getShipmentDetail(shId, userDetails.getMemberId(), ordId);

        return ResponseEntity.ok(updatedShipment);
    }

    @PatchMapping("/{id}") // 관리자 테스트용 배송 현황 수정용
    public ResponseEntity<ShipmentDto> updateShipment(@PathVariable(name="id") Long shId,
                                                      @RequestParam(name="ordId") Long ordId,
                                                      @AuthenticationPrincipal MemberUserDetails userDetails,
                                                      @RequestBody ShipmentDto dto){

        ShipmentDto shipmentDto = shipmentService.updateStatusFromAdmin(shId, ordId, dto);

        return ResponseEntity.ok(shipmentDto);
    }

    @DeleteMapping("/{id}/cancel")
    public ResponseEntity<ShipmentDto> cancelShipment(@PathVariable(name="id") Long shId,
                                                      @RequestParam(name="ordId") Long ordId,
                                                      @AuthenticationPrincipal MemberUserDetails userDetails,
                                                      @RequestBody ShipmentDto dto){

        shipmentService.cancelShipment(shId, ordId, userDetails.getMemberId(), dto.getShStt());

        return ResponseEntity.ok(shipmentService.getShipmentDetail(shId, userDetails.getMemberId(), ordId));
    }

}
