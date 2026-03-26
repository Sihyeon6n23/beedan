package com.goodee.beedan.controller.member;

import com.goodee.beedan.config.web.annotation.Sidebar;
import com.goodee.beedan.dto.member.MemberFormDto;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @GetMapping("/signup")
    public String getSignUp() {
        return "/member/auth/signup";
    }

    @PostMapping("/signup")
    public String postSignUp(
            @Valid @ModelAttribute MemberFormDto memberForm,
            BindingResult bindingResult) {
        // 검증 필요

        return "redirect:/auth/signup";
    }

    @GetMapping("/signin")
    public String getSignIn() {
        return "/member/auth/signin";
    }

    @PostMapping("/signin")
    public String postSignIn() {
        return "redirect:/mypage/detail";
    }

    @PostMapping("/signout")
    public String postSignOut() {
        return "redirect:/auth/signout";
    }

    @GetMapping("/find")
    public String getFind() {
        return "/member/auth/find";
    }
}
