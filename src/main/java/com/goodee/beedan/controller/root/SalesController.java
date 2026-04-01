package com.goodee.beedan.controller.root;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SalesController {

    @GetMapping("/root/sales")
    public String getSales() {
        return "/root/sales/root-sales";
    }
}
