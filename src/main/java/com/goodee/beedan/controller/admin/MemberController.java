package com.goodee.beedan.controller.admin;

import com.goodee.beedan.config.security.MemberUserDetails;
import com.goodee.beedan.dto.admin.MemberEditRequest;
import com.goodee.beedan.dto.admin.MemberEditResponse;
import com.goodee.beedan.service.admin.AdminMemberService;
import com.goodee.beedan.service.member.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/member")
@RequiredArgsConstructor
@Slf4j
public class MemberController {
    private final MemberService memberService;
    private final AdminMemberService adminMemberService;
    @Value("${kakao.map.appkey:}")
    private String kakaoAppKey;

    @GetMapping("/edit")
    public String getMemberEdit(Model model, @RequestParam("id") Long id, @AuthenticationPrincipal MemberUserDetails userDetails){
        if(userDetails.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_ROOT"))){
            throw new IllegalArgumentException();
        }

        MemberEditResponse memberEditResponse = MemberEditResponse.fromEntity(memberService.getMemberById(id));
        model.addAttribute("member", memberEditResponse);
        model.addAttribute("kakaoAppKey", kakaoAppKey);

        return "admin/member/admin-member-edit";
    }

    @PostMapping("/edit")
    public String postMemberEdit(@ModelAttribute MemberEditRequest memberEditRequest,
                                 @AuthenticationPrincipal MemberUserDetails userDetails,
                                 RedirectAttributes redirectAttributes){
        if(userDetails.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_ROOT"))){
            throw new IllegalArgumentException();
        }
        log.info(memberEditRequest.getBizStatus());
        adminMemberService.updateMember(memberEditRequest);
        redirectAttributes.addFlashAttribute("status", "AccountEdit");
        redirectAttributes.addFlashAttribute("message", "정보변경을 성공했습니다.");

        return "redirect:/admin/member/list";
    }

    @GetMapping("/list")
    public String getMemberList() {
        return "admin/member/admin-member-list";
    }

}
