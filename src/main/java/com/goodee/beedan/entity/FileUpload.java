package com.goodee.beedan.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ValueGenerationType;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Builder
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class) // 자동 날짜 입력을 위해 필수
public class FileUpload {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 기본키 설정 및 자동증가
    private Long fileId;

    private String brdRefTy;
    private Long brdRefNo;
    private String fileNm;
    private String fileUuid;
    private String fileExt;
    private String filePat;

    @CreatedBy // 작성자 자동 입력 (Security 설정 시)
    private Long memId;

    @CreatedDate // 생성일 자동 입력
    @Column(updatable = false)
    private LocalDateTime fileCreDt;

    @LastModifiedBy // 수정자 자동 입력
    private Long fileUpdMemId;

    @LastModifiedDate // 수정일 자동 입력
    private LocalDateTime fileUpdDt;

    private Boolean fileDelYn;

    private Integer fileOr;
    private Long fileSz;
    private String fileCtp;
}
