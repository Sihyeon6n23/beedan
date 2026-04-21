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

    private boolean isUploaded;     // 업로드 성공 여부 (Flag)
    private boolean isDeleted;
    private String errorMessage;    // 실패 시 사용자에게 보여줄 사유
}