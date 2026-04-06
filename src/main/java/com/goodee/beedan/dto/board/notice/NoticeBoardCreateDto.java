package com.goodee.beedan.dto.board.notice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoticeBoardCreateDto {
    private String brdTtl;
    private String brdCon;
    private Boolean brdFixYn;
    private List<MultipartFile> files;
}
