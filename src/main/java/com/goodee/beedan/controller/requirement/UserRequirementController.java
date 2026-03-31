package com.goodee.beedan.controller.requirement;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/require")
public class UserRequirementController {

    @GetMapping("/list")
    public String requireList() {
        return "member/requirement/require-list";
    }
}
