package com.goodee.beedan.controller.admin;

import com.goodee.beedan.dto.admin.MemberListDto;
import com.goodee.beedan.service.member.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class AdminMemberApiController {
    private final MemberService memberService;

    @GetMapping("/list")
    public ResponseEntity<Page<MemberListDto>> getMemberList(
            @PageableDefault(size = 10, sort = "memCreDt", direction = Sort.Direction.DESC) Pageable pageable){
        Page<MemberListDto> memberListDtos =  memberService.getAllMembers(pageable);

        return ResponseEntity.ok(memberListDtos);
    }
}
