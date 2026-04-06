package com.goodee.beedan.dto.admin;

import lombok.Builder;
import lombok.Data;


@Data @Builder
public class MemberSearchRequestDto {
    private String memStt;
    private String memBizNo;
    private String bizName;
    private String memNm;
    private String memCeoNm;
}
