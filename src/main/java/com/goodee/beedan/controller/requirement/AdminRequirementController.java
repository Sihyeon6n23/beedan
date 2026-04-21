package com.goodee.beedan.controller.requirement;

import com.goodee.beedan.config.security.MemberUserDetails;
import com.goodee.beedan.dto.requirement.RequireForm;
import com.goodee.beedan.service.requirement.RequirementService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/require")
public class AdminRequirementController {

    private final RequirementService requirementService;

    @GetMapping("/list")
    public String requireList() {
        return "admin/stock/require-list";
    }

    @GetMapping("/detail")
    public String requireDetail(@RequestParam Long id,
                                @AuthenticationPrincipal MemberUserDetails user,
                                Model model) {
        boolean isAdmin = user.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN") || auth.getAuthority().equals("ROLE_ROOT"));

        RequireForm form = requirementService.getRequireForm(id, user.getMemberId(), isAdmin);
        model.addAttribute("require", form);
        model.addAttribute("isAdmin", true);
        return "member/requirement/require-detail";
    }
}
