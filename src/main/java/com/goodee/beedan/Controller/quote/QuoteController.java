package com.goodee.beedan.Controller.quote;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/quote")
public class QuoteController {
    @GetMapping("/list")
    public String getList() {
        return "/quote/quote-list";
    }
}
