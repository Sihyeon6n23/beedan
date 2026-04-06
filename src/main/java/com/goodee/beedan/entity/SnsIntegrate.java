package com.goodee.beedan.entity;

import com.goodee.beedan.common.constant.SnsType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "sns_integrate", indexes = {
        @Index(name = "idx_sns_integrate_mem_id", columnList = "mem_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SnsIntegrate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long snsId; // 컬럼명과 변수명 일치

    @Enumerated(EnumType.STRING)
    private SnsType snsTp;

    private String snsSeNo;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime snsConDt;

    @UpdateTimestamp
    private LocalDateTime snsUpdDt;

    private Boolean snsCanYn;

    // 외래키 제약 조건을 제외한 1:N 연관관계 설정
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "mem_id",
            nullable = false,
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    private Member member;
}
