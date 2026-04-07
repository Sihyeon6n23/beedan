package com.goodee.beedan.dto.file;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FileDto {
    private Long fileId;
    private String fileNm;
    private String fileUuid;
    private String fileExt;
    private Long fileSz;
    private String fileCtp;
    private Integer fileOr;

    private String filePat;
}