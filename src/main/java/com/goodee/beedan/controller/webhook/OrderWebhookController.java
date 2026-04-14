package com.goodee.beedan.controller.webhook;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.goodee.beedan.dto.order.WebhookShipmentRequest;
import com.goodee.beedan.service.order.OrderService;
import com.goodee.beedan.service.webhook.OrderWebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/webhook")
@RequiredArgsConstructor
public class OrderWebhookController {

    private final OrderWebhookService orderWebhookService;
    private final OrderService orderService;

    @Value("${webhook.external.api-key}")
    private String expectedApiKey;

    /**
     * 외부팀에서 송장 정보를 보내올 때 수신
     */
    @PostMapping("/shipment")
    public ResponseEntity<Map<String, Object>> receiveShipment(
            @RequestHeader(value = "X-API-Key", required = false) String apiKey,
            @RequestBody String rawBody) {

        // API 키 검증
        if (apiKey == null || !apiKey.equals(expectedApiKey)) {
            log.warn("webhook 인증 실패. apiKey: {}", apiKey);
            return ResponseEntity.status(401).body(Map.of("status", "error", "message", "Invalid API Key"));
        }

        log.info("외부팀 webhook 수신: {}", rawBody);

        // quId 추출 시도
        Long quId = null;
        try {
            Map<String, Object> parsed = new ObjectMapper().readValue(rawBody, Map.class);
            if (parsed.containsKey("quoteId")) {
                quId = Long.valueOf(parsed.get("quoteId").toString());
            }
        } catch (Exception e) {
            log.error("quId 추출 중 예외 발생: ", e);
        }

        // 로그 저장
        orderWebhookService.logReceive(rawBody, quId);

        try {
            WebhookShipmentRequest webhookRequest = new ObjectMapper().readValue(rawBody, WebhookShipmentRequest.class); // JSON String -> Webhook DTO로 변환

            orderService.createOrderFromWebhook(webhookRequest);

        } catch (Exception e) {
            log.error("웹훅 처리 중 오류 발생: ", e);
        }

        return ResponseEntity.ok(Map.of("status", "ok", "message", "수신 완료"));
    }
}
