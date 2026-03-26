package com.goodee.beedan.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "FACTORY")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Factory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long faId;
    private String faNm;    // 공장명
    private String faAd;    // 공장 주소
    private String faCty;   // 공장 도시
    private String faCCd;   // 국가코드
    private Boolean faYn;   // 활성 여부
    @CreatedDate
    private LocalDateTime faCrDt;
    @LastModifiedDate
    private LocalDateTime faUpDt;

    public void deactivate() {
        this.faYn = false;
    }

    public void update(
            String name,
            String address,
            String city,
            String countryCode
    ){
        this.faNm = name;
        this.faAd = address;
        this.faCty = city;
        this.faCCd = countryCode;
    }

    public boolean isSameCountry(String countryCode) {
        return this.faCCd != null && this.faCCd.equals(countryCode);
    }










}
