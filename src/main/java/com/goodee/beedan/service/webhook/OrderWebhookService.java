package com.goodee.beedan.service.webhook;

import com.goodee.beedan.entity.*;
import com.goodee.beedan.repository.webhook.OrderWebhookLogRepository;
import com.goodee.beedan.service.quote.QuoteBaseService;
import com.goodee.beedan.service.quote.QuoteDetailService;
import com.goodee.beedan.service.quote.NegotiationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderWebhookService {

    private final OrderWebhookLogRepository logRepository;
    private final QuoteBaseService quoteBaseService;
    private final QuoteDetailService quoteDetailService;
    private final NegotiationService negotiationService;

    @Value("${webhook.external.url}")
    private String externalUrl;

    @Value("${webhook.external.api-key}")
    private String apiKey;

    /**
     * 결제 완료 시 외부팀에 상품 정보 전달
     */
    public void sendOrderToExternal(Long quId, Long pyId) {
        QuoteBase quoteBase = quoteBaseService.findById(quId);
        List<QuoteDetail> details = quoteDetailService.findAllByQuote(quId);
        Negotiation negotiation = negotiationService.findById(quoteBase.getNgId());

        // 전달할 JSON 구성
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("quoteId", quId);
        payload.put("quoteCd", quoteBase.getQuCd());
        payload.put("negotiationName", negotiation.getNgNm());

        List<Map<String, Object>> items = new ArrayList<>();
        for (QuoteDetail d : details) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("stockId", d.getStId());
            item.put("stockName", d.getStNm());
            item.put("quantity", d.getQuDtQn());
            item.put("factoryId", d.getFaId());
            item.put("factoryName", d.getFaNm());
            item.put("unitGroupName", d.getUnGNm());
            item.put("unitGroupQty", d.getQuUQn());
            item.put("receiverName", d.getQuDtRcNm());
            item.put("receiverAddress", d.getQuDtRcAdr());
            item.put("receiverPhone", d.getQuDtRcPhn());
            item.put("receiverMemo", d.getQuDtRcMemo());
            items.add(item);
        }
        payload.put("items", items);

        String requestBody = toJson(payload);

        // POST 전송
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-API-Key", apiKey);

            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(externalUrl, entity, String.class);

            // 로그 저장 (성공)
            logRepository.save(OrderWebhookLog.builder()
                    .direction("SEND")
                    .url(externalUrl)
                    .requestBody(requestBody)
                    .responseBody(response.getBody())
                    .httpStatus(response.getStatusCode().value())
                    .quoteId(quId)
                    .paymentId(pyId)
                    .status("SUCCESS")
                    .build());

            log.info("외부팀 webhook 전송 완료. quId: {}, status: {}", quId, response.getStatusCode());

        } catch (Exception e) {
            // 로그 저장 (실패)
            logRepository.save(OrderWebhookLog.builder()
                    .direction("SEND")
                    .url(externalUrl)
                    .requestBody(requestBody)
                    .quoteId(quId)
                    .paymentId(pyId)
                    .status("FAIL")
                    .errorMessage(e.getMessage() != null ? e.getMessage().substring(0, Math.min(e.getMessage().length(), 500)) : "unknown")
                    .build());

            log.error("외부팀 webhook 전송 실패. quId: {}, error: {}", quId, e.getMessage());
        }
    }

    /**
     * 외부팀에서 송장 정보 수신 시 로그 저장
     */
    public OrderWebhookLog logReceive(String requestBody, Long quId) {
        return logRepository.save(OrderWebhookLog.builder()
                .direction("RECEIVE")
                .requestBody(requestBody)
                .quoteId(quId)
                .status("SUCCESS")
                .build());
    }

    private String toJson(Object obj) {
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            return "{}";
        }
    }
}
