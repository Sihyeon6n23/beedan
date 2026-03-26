package com.goodee.beedan.dto.quote;

import com.goodee.beedan.entity.UnitGroup;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UnitGroupResponse {

    private Long unGId;
    private String unGNm;
    private Integer unGQn;
    private Boolean unGYn;
    private LocalDateTime unGCrDt;
    private LocalDateTime unGUpDt;

    public static UnitGroupResponse from(UnitGroup unitGroup) {
        return UnitGroupResponse.builder()
                .unGId(unitGroup.getUnGId())
                .unGNm(unitGroup.getUnGNm())
                .unGQn(unitGroup.getUnGQn())
                .unGYn(unitGroup.getUnGYn())
                .unGCrDt(unitGroup.getUnGCrDt())
                .unGUpDt(unitGroup.getUnGUpDt())
                .build();
    }
}