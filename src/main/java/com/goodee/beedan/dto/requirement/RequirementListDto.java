package com.goodee.beedan.dto.requirement;

import com.goodee.beedan.entity.Member;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class RequirementListDto {
    private Long reqId;
    private Long memId;
    private String reqTtl;
    private String reqStt;
    private Boolean reqRepYn;
    private Boolean reqPerYn;
    private LocalDateTime reqCreDt;


    // 필요한 멤버 추가 정보 (이름, 상태)
    private String memNm;
    private String memStt;
    private String memBizTtl;
}
