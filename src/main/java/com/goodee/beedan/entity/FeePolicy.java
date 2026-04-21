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
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_fp_gr_ty_yn",
                        columnNames = {"bgp_gr", "fp_fee_ty", "fp_ac_yn", "fp_ef_fr_dt"}
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
        private String bgpGr;     // STANDARD PREMIUM VIP
        @Enumerated(EnumType.STRING)
        private FeeCalculationType fpCalcTy;  // RATE FIXED
        @Column(precision = 10, scale = 4, nullable = false)
        private BigDecimal fpVal;// ex) RATE = 5% FIXED = 50,000
        private Boolean fpAcYn; // 활성 여부
        private LocalDateTime fpEfFrDt; // 적용 시작일
        private LocalDateTime fpEfToDt; // 적용 종료일 (null = 무기한)
        private String fpDes; // 관리자 메모
        @CreatedDate
        @Column(updatable = false, nullable = false)
        private LocalDateTime fpCrDt;

        @LastModifiedDate
        @Column(nullable = false)
        private LocalDateTime fpUpDt;

        @PrePersist
        protected void onCreate() {
                if(this.fpAcYn == null) this.fpAcYn = true;
        }

        public void deactivate() {
                this.fpAcYn = false;
                this.fpFeeTy = this.fpFeeTy + "_deleted_" + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
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
                        case RATE -> amount.multiply(this.fpVal)
                                .setScale(0, RoundingMode.HALF_UP);
                        case FIXED -> this.fpVal.setScale(0, RoundingMode.HALF_UP);
                };
        }
}
