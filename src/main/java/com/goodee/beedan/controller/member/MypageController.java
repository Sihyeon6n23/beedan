package com.goodee.beedan.controller.member;

import com.goodee.beedan.config.web.annotation.Sidebar;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/mypage")
public class MypageController {
    @GetMapping("")
    public String getMainRedirect() {
        return "redirect:/mypage/main";
    }

    @GetMapping("/main")
    public String getMain() {
        return "/member/mypage/mypage-main";
    }

    @GetMapping("/detail")
    @Sidebar
    public String getDetail() {
        return "/member/mypage/mypage-detail";
    }

    @GetMapping("/changepw")
    @Sidebar
    public String getChangePw() {
        return "/member/mypage/mypage-changepw";
    }

    @GetMapping("/changebiz")
    @Sidebar
    public String getChangeBiz() {
        return "/member/mypage/mypage-changebiz";
    }
}
