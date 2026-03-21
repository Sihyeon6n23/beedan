package com.goodee.beedan.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileUpload {

    @Id
    private Long fileId;
    private String brdTyp;
    private String brdTtl;
    private String fileNm;
    private String fileUuid;
    private String fileExt;
    private Long memId;
    @CreatedDate
    private LocalDateTime fileCreDt;
    private Long fileUpdMemId;
    @LastModifiedDate
    private LocalDateTime fileUpdDt;
    private Boolean fileDelYn;
    private Integer fileOr;
    private Long fileSz;
    private String fileCtp;
}
