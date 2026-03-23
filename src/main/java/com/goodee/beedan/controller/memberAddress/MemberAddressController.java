package com.goodee.beedan.controller.memberAddress;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MemberAddressController {
    @GetMapping("/member/address")
    public String getMemberAddress(){
        return "member/address/address-test";
    }
}
