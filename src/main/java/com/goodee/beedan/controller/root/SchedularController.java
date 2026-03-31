package com.goodee.beedan.controller.root;

import com.goodee.beedan.dto.root.security.SecurityPolicyDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.io.IOException;

@Controller
@RequiredArgsConstructor
public class SchedularController {

    @GetMapping("/root/schedular")
    public String getSchedular(Model model) throws IOException {
//        model.addAttribute("policy", securityService.getSecPolDto());

        return "/root/schedular/schedular-setting";
    }

    @PostMapping("/root/schedular/save")
    public String postSchedular(@ModelAttribute SecurityPolicyDto secPolDto) throws IOException {

//        securityService.saveSecurityPolicyDto(secPolDto); // 파일에 저장

        return "redirect:/root/schedular"; // 저장 후 다시 페이지로 이동
    }


}
