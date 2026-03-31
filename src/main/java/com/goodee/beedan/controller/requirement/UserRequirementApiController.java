package com.goodee.beedan.controller.requirement;

import com.goodee.beedan.config.security.MemberUserDetails;
import com.goodee.beedan.dto.requirement.RequirementListDto;
import com.goodee.beedan.service.requirement.RequirementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/require")
public class UserRequirementApiController {

    private final RequirementService requirementService;

    @GetMapping("/list")
    public Page<RequirementListDto> list(
            @AuthenticationPrincipal MemberUserDetails user,
            @RequestParam(defaultValue = "ALL") String status,
            @RequestParam(defaultValue = "0") int page) {
        Pageable pageable = PageRequest.of(page, 5, Sort.by(Sort.Direction.DESC,"reqId"));
        Long memId = user.getMemberId();
        return requirementService.findAllForUser(memId, status, pageable);
    }
}
