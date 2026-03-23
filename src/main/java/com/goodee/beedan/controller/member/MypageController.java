package com.goodee.beedan.Controller.member;

import com.goodee.beedan.config.web.annotation.Sidebar;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/mypage")
public class MypageController {
    @GetMapping("")
    @Sidebar
    public String getSignup() {
        return "/member/mypage/mypage-main";
    }
}
