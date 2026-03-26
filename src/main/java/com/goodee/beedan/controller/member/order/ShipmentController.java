package com.goodee.beedan.controller.member.order;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/shipments")
public class ShipmentController {
    @GetMapping("/{id}")
    public String getShipment(@PathVariable("id") Long id){
        return "member/shipment/shipment";
    }
}
