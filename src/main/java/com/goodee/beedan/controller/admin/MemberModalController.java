package com.goodee.beedan.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class MemberModalController {

    @GetMapping("/api/admin/order/{id}")
    public String getMemberOrderList(@PathVariable("id") Long id, Model model){
        return "admin/member/api-admin-order-id";
    }

    @GetMapping("/api/admin/shipment/{id}")
    public String getMemberShipmentList(@PathVariable("id") Long id, Model model){
        return "admin/member/api-admin-shipment-id";
    }

    @GetMapping("/api/admin/inquiry/{id}")
    public String getMemberInquiryList(@PathVariable("id") Long id, Model model){
        return "admin/member/api-admin-inquiry-id";
    }

}
