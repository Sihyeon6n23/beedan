package com.goodee.beedan.dto.file;

import lombok.Builder;
import lombok.Data;
import org.springframework.core.io.Resource;

@Data
@Builder
public class FileDownloadDto {
    private String fileName;      // 원래 파일명 (한글 깨짐 방지용)
    private String contentType;   // MIME 타입 (image/png 등)
    private Long contentLength;   // 파일 크기 (진행률 표시용)
    private Resource resource;    // 실제 파일 리소스 (알맹이)
}
