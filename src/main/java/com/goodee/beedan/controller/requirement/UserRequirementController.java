package com.goodee.beedan.controller.requirement;

import com.goodee.beedan.config.security.MemberUserDetails;
import com.goodee.beedan.dto.requirement.RequireForm;
import com.goodee.beedan.service.member.MemberService;
import com.goodee.beedan.service.requirement.RequirementService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/require")
public class UserRequirementController {

    private final MemberService memberService;
    private final RequirementService requirementService;

    @GetMapping("/list")
    public String requireList() {
        return "member/requirement/require-list";
    }

    @GetMapping("/write")
    public String writeRequirement(@RequestParam(required = false) Long reqId,
                                   @AuthenticationPrincipal MemberUserDetails user,
                                   Model model) {
        boolean isAdmin = user.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        if (reqId != null) {
            model.addAttribute("require", requirementService.getRequireForm(reqId, user.getMemberId(), isAdmin));
        } else {
            model.addAttribute("require", new RequireForm());
        }
        return "member/requirement/require-write";
    }

    @PostMapping("/write")
    public String writeRequirement(RequireForm requireForm,
                                   @AuthenticationPrincipal MemberUserDetails user,
                                   Model model) {
        requirementService.submitRequirement(user.getMemberId(), requireForm);
        return "redirect:/require/list";
    }

    @GetMapping("/detail")
    public String requireDetail(@RequestParam Long id,
                                @AuthenticationPrincipal MemberUserDetails user,
                                Model model) {
        boolean isAdmin = user.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN") || auth.getAuthority().equals("ROLE_ROOT"));

            RequireForm form = requirementService.getRequireForm(id, user.getMemberId(), isAdmin);

            model.addAttribute("require", form);
            model.addAttribute("isAdmin", false);
            return "member/requirement/require-detail";

    }

}
