package com.example.beedan.controller.member;

import com.example.beedan.common.annotation.Sidebar;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MemberController {
    @GetMapping("/member")
    @Sidebar
    public String getSignup() {
        return "/member/mypage/signup";
    }
}
