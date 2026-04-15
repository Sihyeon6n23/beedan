package com.goodee.beedan.controller.root;

import com.goodee.beedan.dto.member.MemberApproveDto;
import com.goodee.beedan.service.member.MemberService; // 서비스 구현 필요
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/root/member")
@RequiredArgsConstructor
@Slf4j
public class MemberApproveController {

    private final MemberService memberService;

    @GetMapping("/approve")
    public String approvePage(Model model) {
        List<MemberApproveDto> pendingMembers = memberService.findBizPendingMembersWithFiles();
        model.addAttribute("members", pendingMembers);
        return "root/member/approve";
    }
}