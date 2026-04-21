package com.goodee.beedan.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "order_webhook_log")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class OrderWebhookLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long owlId;

    @Column(length = 10, nullable = false)
    private String owlDir;          // SEND / RECEIVE

    @Column(length = 500)
    private String owlUrl;          // 요청 URL

    @Column(columnDefinition = "TEXT")
    private String owlReqBody;      // 요청 JSON

    @Column(columnDefinition = "TEXT")
    private String owlResBody;      // 응답 JSON

    private Integer owlHttpStt;     // HTTP 상태코드

    private Long quId;              // 관련 견적 ID

    private Long pyId;              // 관련 결제 ID

    @Column(length = 20)
    private String owlStt;          // SUCCESS / FAIL

    @Column(length = 500)
    private String owlErrMsg;       // 실패 시 에러 메시지

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime owlCreDt;

    @Builder
    public OrderWebhookLog(String direction, String url, String requestBody,
                           String responseBody, Integer httpStatus,
                           Long quoteId, Long paymentId, String status, String errorMessage) {
        this.owlDir = direction;
        this.owlUrl = url;
        this.owlReqBody = requestBody;
        this.owlResBody = responseBody;
        this.owlHttpStt = httpStatus;
        this.quId = quoteId;
        this.pyId = paymentId;
        this.owlStt = status;
        this.owlErrMsg = errorMessage;
    }
}
