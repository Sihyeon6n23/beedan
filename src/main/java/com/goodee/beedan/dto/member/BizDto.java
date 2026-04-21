package com.goodee.beedan.dto.member;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@Builder
@ToString
public class BizDto {
    @JsonProperty("valid")
    private String valid;
    @JsonProperty("b_no")
    private String bNo;
    @JsonProperty("p_nm")
    private String pNm;
    @JsonProperty("start_dt")
    private String startDt;
    @JsonProperty("b_nm")
    private String bNm;
}
