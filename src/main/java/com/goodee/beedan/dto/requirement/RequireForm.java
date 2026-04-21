package com.goodee.beedan.dto.requirement;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RequireForm {
    private Long reqId;
    private Long memId;
    private String memNm;
    private String memBizTtl;
    private String reqTtl;
    private String reqCnt;
    private String reqRef;
    private BigDecimal reqPr;
    private String reqCur;
    private String reqStt;
    private Boolean reqPerYn;
    private Boolean reqRepYn;

    // 답변 정보
    private Long reqRepId;
    private String reqRepTtl;
    private String reqRepCon;
    private Boolean reqRepPerYn;
}
