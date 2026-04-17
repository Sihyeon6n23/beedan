package com.goodee.beedan.controller.requirement;

import com.goodee.beedan.config.security.MemberUserDetails;
import com.goodee.beedan.dto.requirement.RequireForm;
import com.goodee.beedan.dto.requirement.RequirementListDto;
import com.goodee.beedan.service.requirement.RequirementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/require")
public class UserRequirementApiController {

    private final RequirementService requirementService;

    @GetMapping("/list")
    public Page<RequirementListDto> list(
            @AuthenticationPrincipal MemberUserDetails user,
            @RequestParam(defaultValue = "ALL") String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page) {
        Pageable pageable = PageRequest.of(page, 5, Sort.by(Sort.Direction.DESC,"reqId"));
        Long memId = user.getMemberId();
        return requirementService.findAllForUser(memId, status, keyword, pageable);
    }

    // 임시저장 비동기
    @PostMapping("/draft")
    public Long draftRequirement(RequireForm requireForm,
                                 @AuthenticationPrincipal MemberUserDetails user) {
        return requirementService.draftRequirement(user.getMemberId(), requireForm);
    }

    // DRAFT 삭제
    @DeleteMapping("/{reqId}")
    public void deleteRequirement(@PathVariable Long reqId,
                                  @AuthenticationPrincipal MemberUserDetails user) {
        requirementService.deleteRequirement(reqId, user.getMemberId());
    }

    // DRAFT → SUBMITTED 제출
    @PostMapping("/{reqId}/submit")
    public void submitDraft(@PathVariable Long reqId,
                            @AuthenticationPrincipal MemberUserDetails user) {
        requirementService.submitDraft(reqId, user.getMemberId());
    }

    // SUBMITTED → 등록 취소
    @PostMapping("/{reqId}/cancel")
    public void cancelRequirement(@PathVariable Long reqId,
                                  @AuthenticationPrincipal MemberUserDetails user) {
        requirementService.cancelRequirement(reqId, user.getMemberId());
    }
}


