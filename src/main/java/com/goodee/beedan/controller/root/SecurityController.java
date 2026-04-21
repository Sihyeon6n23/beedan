package com.goodee.beedan.controller.root;

import com.goodee.beedan.dto.root.security.SecurityPolicyDto;
import com.goodee.beedan.service.root.SecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class SecurityController {
    private final SecurityService securityService;

    @GetMapping("/root/security")
    public String getSecurity(Model model)  {
        model.addAttribute("policy", securityService.getSecPolDto());

        return "/root/security/security-policy";
    }

    @PostMapping("/root/security/save")
    public String postSecurity(@ModelAttribute SecurityPolicyDto secPolDto)  {

        securityService.saveSecurityPolicyDto(secPolDto); // 파일에 저장

        return "redirect:/root/security"; // 저장 후 다시 페이지로 이동
    }


}
