package com.goodee.beedan.controller.receiver;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/receiver")
public class ReceiverController {
    @GetMapping
    public String getReceiverPage() {
        return "receiver/receiver";
    }
}
