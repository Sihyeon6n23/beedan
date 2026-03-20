package com.example.beedan.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MemberController {
    @GetMapping("/member")
    public String getSignup() {
        return "/member/mypage/signup";
    }
}
