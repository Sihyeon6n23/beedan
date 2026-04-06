package com.goodee.beedan.controller.payment;

import com.goodee.beedan.common.constant.PaymentMethod;
import com.goodee.beedan.common.constant.PaymentStatus;
import com.goodee.beedan.config.security.MemberUserDetails;
import com.goodee.beedan.entity.Payment;
import com.goodee.beedan.entity.QuoteBase;
import com.goodee.beedan.entity.QuoteInfo;
import com.goodee.beedan.repository.payment.PaymentRepository;
import com.goodee.beedan.repository.quote.QuoteInfoRepository;
import com.goodee.beedan.service.quote.QuoteBaseService;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentRestController {

    private final PaymentRepository paymentRepository;
    private final QuoteBaseService quoteBaseService;
    private final QuoteInfoRepository quoteInfoRepository;

    @Value("${toss.payments.secret-key}")
    private String tossSecretKey;

    @Transactional
    @PostMapping("/confirm")
    public ResponseEntity<Map<String, Object>> confirmPayment(
            @RequestBody ConfirmRequest request,
            @AuthenticationPrincipal MemberUserDetails userDetails) {
        try {
            if (userDetails == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("status", "error", "message", "로그인이 필요합니다."));
            }

            // 1. 토스페이먼츠 결제 승인 API 호출
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String encoded = Base64.getEncoder()
                    .encodeToString((tossSecretKey + ":").getBytes(StandardCharsets.UTF_8));
            headers.set("Authorization", "Basic " + encoded);

            Map<String, Object> tossBody = Map.of(
                    "paymentKey", request.getPaymentKey(),
                    "orderId", request.getOrderId(),
                    "amount", request.getAmount()
            );

            HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>(tossBody, headers);
            ResponseEntity<Map> tossResponse;
            try {
                tossResponse = restTemplate.postForEntity(
                        "https://api.tosspayments.com/v1/payments/confirm",
                        httpEntity, Map.class);
            } catch (Exception e) {
                log.error("토스 결제 승인 실패: {}", e.getMessage());
                return ResponseEntity.badRequest()
                        .body(Map.of("status", "error", "message", "결제 승인 실패: " + e.getMessage()));
            }

            if (!tossResponse.getStatusCode().is2xxSuccessful()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("status", "error", "message", "토스 결제 승인 실패"));
            }

            // 2. 견적 정보 조회
            QuoteBase quoteBase = quoteBaseService.findById(request.getQuId());
            QuoteInfo quoteInfo = quoteInfoRepository.findByQuId(request.getQuId()).orElse(null);

            // 결제 수단 판별
            PaymentMethod method = "카드".equals(request.getMethod()) || "CARD".equalsIgnoreCase(request.getMethod())
                    ? PaymentMethod.CARD : PaymentMethod.VIRTUAL_ACCOUNT;

            // 3. Payment 엔티티 생성 및 저장
            Payment payment = Payment.builder()
                    .quId(request.getQuId())
                    .ngId(quoteBase.getNgId())
                    .memId(userDetails.getMemberId())
                    .paymentMethod(method)
                    .totalAmount(BigDecimal.valueOf(request.getAmount()))
                    .supplyValue(quoteInfo != null && quoteInfo.getQuInfoTp() != null
                            ? quoteInfo.getQuInfoTp() : BigDecimal.ZERO)
                    .pgTransactionId(request.getPaymentKey())
                    .build();
            payment.confirmPayment(request.getPaymentKey());
            paymentRepository.save(payment);

            log.info("결제 완료. paymentId: {}, quId: {}, amount: {}",
                    payment.getPyId(), request.getQuId(), request.getAmount());

            return ResponseEntity.ok(Map.of(
                    "status", "ok",
                    "redirectUrl", "/payment/quote-detail?quId=" + request.getQuId()
                            + "&paymentKey=" + request.getPaymentKey()
                            + "&orderId=" + request.getOrderId()
            ));

        } catch (Exception e) {
            log.error("결제 처리 실패: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("status", "error", "message", "결제 처리 실패: " + e.getMessage()));
        }
    }

    @Getter
    @NoArgsConstructor
    public static class ConfirmRequest {
        private Long quId;
        private String paymentKey;
        private String orderId;
        private Long amount;
        private String method; // "카드" or "가상계좌"
    }
}
