package com.goodee.beedan.controller.requirement;

import com.goodee.beedan.config.security.MemberUserDetails;
import com.goodee.beedan.service.member.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/require")
public class UserRequirementController {

    private final MemberService memberService;
    @GetMapping("/list")
    public String requireList() {
        return "member/requirement/require-list";
    }

    @GetMapping("/write")
    public String writeRequirement(@AuthenticationPrincipal MemberUserDetails user,
                                    Model model) {

        return "member/requirement/require-write";
    }
}
