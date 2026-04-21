package com.goodee.beedan.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Factory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fa_id")
    private Long faId;
    @Column(name = "br_id")
    private Long brId;
    @Column(name = "fa_nm", length = 100)
    private String faNm;    // 공장명
    @Column(name = "fa_ad", length = 255)
    private String faAd;    // 공장 주소
    @Column(name = "fa_cty")
    private String faCty;   // 공장 도시
    @Column(name = "fa_c_cd", length = 10)
    private String faCCd;   // 국가코드
    @Column(name = "fa_yn")
    private Boolean faYn;   // 활성 여부
    @CreatedDate
    @Column(name = "fa_cr_dt", updatable = false, nullable = false)
    private LocalDateTime faCrDt;
    @LastModifiedDate
    @Column(name = "fa_up_dt", nullable = false)
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


    @PrePersist
    protected void onCreate() {
        if (this.faYn == null) this.faYn = true;
    }







}
