package com.goodee.beedan.dto.admin;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data @Builder
public class MemberListDto {
    private Long memId;
    private String memEml;
    private String memLgnId;
    private String memBizTtl;
    private String memCeoNm;
    private String memBizAdr;
    private String memBizNo;
    private String memStt;
    private LocalDateTime memCreDt;
    private String memNm;
}
