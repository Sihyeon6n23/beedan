package com.goodee.beedan.dto.board.notice;

import lombok.Data;

import java.util.List;

@Data
public class BoardResultMessage {
    private Long deleteSuccess;
    private Long uploadSuccess;
    private Long fail;
    private List<String> failReason;
}
