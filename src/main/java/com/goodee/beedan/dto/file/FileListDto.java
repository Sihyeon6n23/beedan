package com.goodee.beedan.dto.file;

import lombok.Builder;
import lombok.Data;
import org.springframework.core.io.Resource;

@Data
@Builder
public class FileListDto {
    private String originalName;
    private String uuid;
    private Long fileSize;
    private String fileExt;
    private String filePat;
}
