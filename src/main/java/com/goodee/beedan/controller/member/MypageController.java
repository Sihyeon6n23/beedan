package com.goodee.beedan.controller.member;

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
    public String getDetail() {
        return "/member/mypage/mypage-detail";
    }

    @GetMapping("/changepw")
    public String getChangePw() {
        return "/member/mypage/mypage-changepw";
    }

    @GetMapping("/changebiz")
    public String getChangeBiz() {
        return "/member/mypage/mypage-changebiz";
    }
}
