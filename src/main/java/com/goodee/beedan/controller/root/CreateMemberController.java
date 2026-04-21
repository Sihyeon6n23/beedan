package com.goodee.beedan.controller.root;

import com.goodee.beedan.dto.member.MemberCreateFormDto;
import com.goodee.beedan.dto.member.MemberFormDto;
import com.goodee.beedan.service.member.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/root/member")
@RequiredArgsConstructor
@Slf4j
public class CreateMemberController {
    private final MemberService memberService;
    @GetMapping("/create")
    public String getCreateMember(Model model) {
        model.addAttribute("memberCreateFormDto", new MemberCreateFormDto());
        return "root/member/create";
    }

    @PostMapping("/create")
    public String postCreateMember(Model model,
                                   @Valid @ModelAttribute MemberCreateFormDto memberCreateFormDto,
                                   BindingResult bindingResult,
                                   RedirectAttributes rttr) {
        if (bindingResult.hasErrors()) {
            return "root/member/create";
        }

        if (!memberCreateFormDto.isPasswordMatching()) {
            bindingResult.rejectValue("confirmPassword", "passwordIncorect", "비밀번호가 일치하지 않습니다.");
            return "root/member/create";
        }

        try {
            memberService.saveMember(memberCreateFormDto);
        } catch (IllegalStateException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "root/member/create";
        }

        rttr.addFlashAttribute("status", "AccountCreate");
        rttr.addFlashAttribute("message", "관리자 계정 생성을 성공했습니다.");
        return "redirect:/root/main";
    }
}
