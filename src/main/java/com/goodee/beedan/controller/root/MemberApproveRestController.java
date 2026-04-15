package com.goodee.beedan.controller.root;

import com.goodee.beedan.service.member.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/root/member")
@RequiredArgsConstructor
public class MemberApproveRestController {

    private final MemberService memberService;

    @PostMapping("/approve/{memId}")
    public ResponseEntity<String> approveMember(@PathVariable Long memId) {
        memberService.approveAccount(memId);
        return ResponseEntity.ok("Approved");
    }

    @PostMapping("/deny/{memId}")
    public ResponseEntity<String> denyMember(@PathVariable Long memId) {
        memberService.rejectAccount(memId);
        return ResponseEntity.ok("Denied");
    }
}