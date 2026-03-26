package com.goodee.beedan.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CrawlingUrl {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long urlId;
    private String urlUrl;
    private Long brId;
    private Long catId;

    // 저장된 셀렉터 (최초 1회 입력 후 재사용)
    private String urlSelItem;
    private String urlSelNm;
    private String urlSelPr;
    private String urlSelImg;
    private String urlCur;

    // 크롤링 방식
    private String urlTy;

    private LocalDateTime urlUpdDt;
    private boolean urlUseYn;
    private boolean urlDelYn;
    private boolean urlAtYn;
}
