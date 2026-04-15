package com.goodee.beedan.controller.error;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/error")
@RequiredArgsConstructor
public class ErrorController {

    @GetMapping("/denied")
    public String accessDenied() {
        return "error/common";
    }
}
