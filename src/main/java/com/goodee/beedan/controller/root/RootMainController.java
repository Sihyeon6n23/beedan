package com.goodee.beedan.controller.root;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RootMainController {
    @GetMapping("/root/main")
    public String getMemberEdit(){
        return "root/root-main";
    }
}
