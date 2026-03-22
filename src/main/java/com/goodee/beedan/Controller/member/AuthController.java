package com.goodee.beedan.Controller.member;

import com.goodee.beedan.config.web.annotation.Sidebar;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @GetMapping("/signup")
    public String getSignup() {
        return "/member/auth/signup";
    }

    @PostMapping("/signup")
    public String postSignup() {
        return "redirect:/auth/signup";
    }

    @GetMapping("/signin")
    public String getSignin() {
        return "/member/auth/signin";
    }

    @PostMapping("/signup")
    public String postSignin() {
        return "redirect:/auth/signin";
    }

    @GetMapping("/find")
    public String getFind() {
        return "/member/auth/find";
    }
}


