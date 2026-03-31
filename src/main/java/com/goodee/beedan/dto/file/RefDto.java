package com.goodee.beedan.dto.file;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RefDto {
    private String refTy;
    private Long refNo;
}
