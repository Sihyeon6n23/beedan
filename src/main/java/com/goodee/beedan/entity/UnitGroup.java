package com.goodee.beedan.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "UNIT_GROUP")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class UnitGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long unGId;
    @Column(nullable = false, unique = true)
    private String unGNm;   // 단위명
    @Column(nullable = false)
    private Integer unGQn;  // 단위당 수량
    private Boolean unGYn;
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime unGCrDt;
    @LastModifiedDate
    private LocalDateTime unGUpDt;

    public void deactivate() {
        this.unGYn = false;
    }

    public void update(String name, Integer quantity) {
        this.unGNm = name;
        this.unGQn = quantity;
    }

    public Integer calculateEachQty(Integer unitCount) {
        if (unitCount == null || unitCount <= 0) return 0;
        return unitCount * this.unGQn;
    }

}
