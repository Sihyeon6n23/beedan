package com.goodee.beedan.controller.root;

import com.goodee.beedan.common.constant.MemberStatus;
import com.goodee.beedan.dto.member.MemberApproveDto;
import com.goodee.beedan.service.member.MemberService; // 서비스 구현 필요
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/root/member")
@RequiredArgsConstructor
public class MemberApproveController {

    private final MemberService memberService;

    @GetMapping("/approve")
    public String approvePage(Model model) {
        // 서비스에서 MemberStatus.PENDING 인 회원과 SIGNUP 타입의 파일을 조인해서 가져옴
        List<MemberApproveDto> pendingMembers = memberService.findPendingMembersWithFiles();
        model.addAttribute("members", pendingMembers);
        return "root/member/approve";
    }
}