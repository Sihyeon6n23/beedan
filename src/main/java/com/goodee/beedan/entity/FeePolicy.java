package com.goodee.beedan.entity;

import com.goodee.beedan.common.policy.FeeCalculationType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "FEE_POLICY",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_fp_gr_ty_yn",
                        columnNames = {"bgp_gr", "fp_fee_ty", "fp_ac_ym", "fp_ef_fr_dt"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class FeePolicy {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long fpId;
        @Column(length = 50, nullable = false)
        private String fpFeeTy; // SERVICE_COMMISION, SHIPPING
        @Column(length = 20, nullable = false)
        private Long bgpGr;     // STANDARD PREMIUM VIP
        @Column(nullable = false)
        private Enum fpCalcTy;  // RATE FIXED
        @Column(precision = 10, scale = 4, nullable = false)
        private BigDecimal fpVal;// ex) RATE = 5% FIXED = 50,000
        @Column(nullable = false)
        private Boolean fpAcYn; // 활성 여부
        @Column(nullable = false)
        private LocalDateTime fpEfFrDt; // 적용 시작일
        @Column(nullable = false)
        private LocalDateTime fpEfToDt; // 적용 종료일 (null = 무기한)
        private String fpDes; // 관리자 메모
        @CreatedDate
        @Column(updatable = false, nullable = false)
        private LocalDateTime fpCrDt;

        @LastModifiedDate
        @Column(nullable = false)
        private LocalDateTime fpUpDt;


        public void deactivate() {
                this.fpAcYn = false;
        }

        public void update(
                String calculationType,
                BigDecimal value,
                LocalDateTime effectFromtDate,
                LocalDateTime effectToDate,
                String description
        ) {
                this.fpCalcTy = FeeCalculationType.valueOf(calculationType);
                this.fpVal = value;
                this.fpEfFrDt = effectFromtDate;
                this.fpEfToDt = effectToDate;
                this.fpDes = description;
        }

        public boolean isValid() {
                if (!Boolean.TRUE.equals(this.fpAcYn)) return false;

                LocalDateTime now = LocalDateTime.now();
                if (this.fpEfFrDt != null && now.isBefore(this.fpEfFrDt)) return false;
                if (this.fpEfToDt != null && now.isAfter(this.fpEfToDt)) return false;

                return true;

                }
public BigDecimal apply(BigDecimal amount) {
        return
                switch (this.fpCalcTy) {
                        case FeeCalculationType.RATE -> amount.multiply(this.fpVal)
                                .setScale(0, RoundingMode.HALF_UP);
                        case FeeCalculationType.FIXED -> this.fpVal.setScale(0, RoundingMode.HALF_UP);
                        default -> throw new IllegalStateException(
                                "알 수 없는 계산 방식: " + this.fpCalcTy);
                };
        }
}
