package com.goodee.beedan.dto.member;

import lombok.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberApproveDto {
    private Long memId;
    private String memNm;           // 이름
    private String memBizNo;        // 사업자 번호 (원본)
    private LocalDateTime memCreDt; // 가입 요청일
    private String filePath;
    private String fileUuid;

    // 사업자 번호 포맷팅 (3-2-5 자리)
    public String getFormattedBizNo() {
        if (memBizNo != null && memBizNo.length() == 10) {
            return memBizNo.substring(0, 3) + "-" +
                    memBizNo.substring(3, 5) + "-" +
                    memBizNo.substring(5);
        }
        return memBizNo;
    }

    // 가입일 포맷팅
    public String getFormattedDate() {
        if (memCreDt == null) return "";
        return memCreDt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
}