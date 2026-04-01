package com.goodee.beedan.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Entity
@Table(name = "QU_INFO")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class QuoteInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long quInfoId;
    private Long quId;  //QuoteBase
    private Long ngId;
    @Column(precision = 18, scale = 6)
    private BigDecimal quInfoExcRt; // 적용 환율
    private String quInfoCurCd; // 통화 코드
    private Long bgpId; // 적용 등급
    private Long fpId;  // 적용 비용 정책

    @Column(precision = 18, scale = 0)
    private BigDecimal quInfoSrvFe; // 서비스 수수료
    @Column(name = "qu_info_srv_fe_r", precision = 10, scale = 4)
    private BigDecimal quInfoSrvFeR;    // 서비스 수수료 할인율
    @Column(precision = 18, scale = 0)
    private BigDecimal quInfoSrvFeAm;   // 할인 후 서비스 수수료

    @Column(precision = 18, scale = 0)
    private BigDecimal quInfoDdAm;  // 국내 배송비
    @Column(precision = 18, scale = 0)
    private BigDecimal quInfoDdExAm;    // 도서산간 추가 배송비

    @Column(precision = 18, scale = 0)
    private BigDecimal quInfoIntShiFe;  // 국제 배송비 합계
    @Column(precision = 18, scale = 0)
    private BigDecimal quInfoDomShiFe;  // 국내 배송비 합계

    @Column(precision = 18, scale = 0)
    private BigDecimal quInfoTtlShiFe;  // 전체 배송비 합계

    @Column(precision = 18, scale = 0)
    private BigDecimal quInfoTax;   // 관부가세 합계
    @Column(precision = 18, scale = 0)
    private BigDecimal quInfoTp;    // 최종 합계
    @Column(precision = 18, scale = 0)
    private BigDecimal quInfoDisTp; // 할인된 총 금액
    private String quInfoPs;    // 메모/특이사항
    private LocalDateTime quInfoDsrDt;  // 희망 수령일
    private Long siId;  // 선택한 보험 ID
    private Long stiId; // 선택한 검사 ID

    @Builder
    public QuoteInfo(
            Long quoteId,
            Long negoId,
            String currencyCode,
            BigDecimal exchangeRate,
            Long buyerGradePolicyId,
            Long feePolicyId,
            String ps,
            LocalDateTime desiredDate
    ){
        this.quId = quoteId;
        this.ngId = negoId;
        this.quInfoCurCd = currencyCode;
        this.quInfoExcRt = exchangeRate;
        this.bgpId = buyerGradePolicyId;
        this.fpId = feePolicyId;
        this.quInfoPs = ps;
        this.quInfoDsrDt = desiredDate;
    }

    public void updateDraft(String memo, Long insuranceId, Long inspectionId) {
        this.quInfoPs = memo;
        this.siId = insuranceId;
        this.stiId = inspectionId;
    }

    public void calculateServiceFee(
            BigDecimal itemTotal,
            BigDecimal feeRate,
            BigDecimal discountRate
    ){
        this.quInfoSrvFe = itemTotal.multiply(feeRate)
                .setScale(0, RoundingMode.HALF_UP);
        this.quInfoSrvFeR = discountRate;
        this.quInfoSrvFeAm = this.quInfoSrvFe
                .multiply(BigDecimal.ONE.subtract(discountRate))
                .setScale(0, RoundingMode.HALF_UP);
    }

    public void setDomesticDelivery(
            BigDecimal amount,
            BigDecimal extraAmount
    ){
        this.quInfoDdAm = amount;
        this.quInfoDdExAm = extraAmount != null
                ? extraAmount
                : BigDecimal.ZERO;
    }

    public void calculateTotal(
            BigDecimal itemTotal,
            BigDecimal intShipTotal,
            BigDecimal taxTotal
    ){
        this.quInfoIntShiFe = intShipTotal;
        this.quInfoDomShiFe = (
                this.quInfoDdAm != null
                        ? this.quInfoDdAm
                        : BigDecimal.ZERO
        ).add(
                this.quInfoDdExAm != null
                        ? this.quInfoDdExAm
                        : BigDecimal.ZERO
        );

        this.quInfoTtlShiFe = this.quInfoIntShiFe.add(this.quInfoDomShiFe);

        this.quInfoTax = taxTotal;

        this.quInfoTp = itemTotal
                .add(this.quInfoTtlShiFe)
                .add(this.quInfoSrvFeAm != null
                        ? this.quInfoSrvFeAm
                        : BigDecimal.ZERO)
                .add(taxTotal);
    }
}
