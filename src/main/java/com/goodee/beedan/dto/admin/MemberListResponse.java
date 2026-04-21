package com.goodee.beedan.dto.admin;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Page;

@Getter @AllArgsConstructor
public class MemberListResponse {
    private Page<MemberListDto> memberList;
    @JsonProperty("isRoot")
    private boolean isRoot;
}
