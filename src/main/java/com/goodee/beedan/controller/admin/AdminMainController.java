package com.goodee.beedan.controller.admin;

import com.goodee.beedan.dto.admin.MemberEditResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AdminMainController {
    @GetMapping("/admin/main")
    public String getMemberEdit(){
        return "admin/admin-main";
    }
}
