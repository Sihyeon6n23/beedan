package com.goodee.beedan.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin/member")
@RequiredArgsConstructor
public class MemberController {
    @Value("${kakao.map.appkey}")
    private String kakaoAppKey;

    @GetMapping("/list")
    public String getMemberList(){
    return "/admin/member/admin-member-list";
    }

    @GetMapping("/detail")
    public String getMemberDetail(@RequestParam(name = "id") Long id, Model model){
        model.addAttribute("kakaoAppKey", kakaoAppKey);
        return "admin/member/admin-member-edit";
    }

}
