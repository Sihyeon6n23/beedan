package com.goodee.beedan.controller.quote;

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

    @GetMapping("/write")
    public String getWrite() {
        return "/quote/quote-write";
    }

    @GetMapping("/detail")
    public String getDetail() { return "/quote/quote-detail"; }

}
