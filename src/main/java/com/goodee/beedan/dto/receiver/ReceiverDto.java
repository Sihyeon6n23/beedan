package com.goodee.beedan.dto.receiver;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class ReceiverDto {
    private Long rcId;
    private String rcNm;
    private String rcPhn;
    private String rcMsg;
    private String rcAdr;
    private String rcAdrDt;

    private Long mem_id;
}
