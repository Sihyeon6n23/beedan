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
public class UnitGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "un_g_id")
    private Long unGId;
    @Column(name = "un_g_nm", nullable = false, unique = true)
    private String unGNm;   // 단위명
    @Column(name = "un_g_qn", nullable = false)
    private Integer unGQn;  // 단위당 수량
    @Column(name = "un_g_yn")
    private Boolean unGYn;
    @CreatedDate
    @Column(name = "un_g_cr_dt", updatable = false)
    private LocalDateTime unGCrDt;
    @LastModifiedDate
    @Column(name = "un_g_up_dt")
    private LocalDateTime unGUpDt;

    public void deactivate() {
        this.unGYn = false;
        this.unGNm = this.unGNm + "_deleted_" + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    public void update(String name, Integer quantity) {
        this.unGNm = name;
        this.unGQn = quantity;
    }

    public Integer calculateEachQty(Integer unitCount) {
        if (unitCount == null || unitCount <= 0) return 0;
        return unitCount * this.unGQn;
    }
    @PrePersist
    protected void onCreate() {
        if (this.unGYn == null) this.unGYn = true;
    }

}
