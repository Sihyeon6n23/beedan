package com.goodee.beedan.entity;

import com.goodee.beedan.common.constant.PaymentMethod;
import com.goodee.beedan.common.constant.PaymentStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pyId;

    private Long quId;      // 견적 아이디
    private Long ngId;      // 협상 아이디
    private Long memId;     // 결제자 아이디
    private Long pyNm;      // 결제 번호

    @Enumerated(EnumType.STRING)
    private PaymentMethod pyMt;    // 결제 수단

    @Enumerated(EnumType.STRING)
    private PaymentStatus pyStt;   // 결제 상태

    @Column(columnDefinition = "TEXT")
    private String pyRs;           // 결제 취소 사유

    private LocalDateTime pyPdAt;  // 결제 완료 일시

    @Column(precision = 18, scale = 0)
    private BigDecimal pyTtAm;     // 총 결제 금액

    @Column(precision = 18, scale = 0)
    private BigDecimal pySpVl;     // 총 공급가액

    @Column(length = 200)
    private String pyPgNm;         // PG사 거래 번호 (paymentKey)

    @Column(length = 20)
    private String pyVbNm;         // 가상계좌 번호

    private Boolean pyTxbYn;       // 세금계산서 신청 여부

    @Column(length = 50)
    private String pyInfoTaxNm;    // 세금계산서 승인 번호

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime pyCreDt;

    @Builder
    public Payment(Long quId, Long ngId, Long memId,
                   PaymentMethod paymentMethod, BigDecimal totalAmount,
                   BigDecimal supplyValue, String pgTransactionId) {
        this.quId = quId;
        this.ngId = ngId;
        this.memId = memId;
        this.pyMt = paymentMethod;
        this.pyTtAm = totalAmount;
        this.pySpVl = supplyValue;
        this.pyPgNm = pgTransactionId;
        this.pyStt = PaymentStatus.READY;
        this.pyTxbYn = false;
    }

    public void confirmPayment(String paymentKey) {
        this.pyStt = PaymentStatus.PAID;
        this.pyPdAt = LocalDateTime.now();
        this.pyPgNm = paymentKey;
    }

    public void failPayment(String reason) {
        this.pyStt = PaymentStatus.FAILED;
        this.pyRs = reason;
    }

    public void cancelPayment(String reason) {
        this.pyStt = PaymentStatus.CANCELLED;
        this.pyRs = reason;
    }
}
