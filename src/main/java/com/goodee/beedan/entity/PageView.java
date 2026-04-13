package com.goodee.beedan.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class PageView {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pvId;

    private String pvPage;       // 페이지 식별자 (STOCK_DETAIL, CART, QUOTE_WRITE 등)
    private Long pvRefId;        // 참조 ID (상품 ID 등, null 가능)
    private Long pvMemId;        // 회원 ID (비로그인이면 null)
    private Integer pvDwellSec;  // 체류 시간 (초)

    @CreatedDate
    private LocalDateTime pvCreDt;

    @Builder
    public PageView(String page, Long refId, Long memId) {
        this.pvPage = page;
        this.pvRefId = refId;
        this.pvMemId = memId;
    }

    public void setDwellSec(Integer sec) {
        this.pvDwellSec = sec;
    }
}
