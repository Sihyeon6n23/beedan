package com.goodee.beedan.service.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.goodee.beedan.dto.admin.MemberSummaryDto;
import com.goodee.beedan.dto.order.TrackingResponseDto;
import com.goodee.beedan.entity.Shipment;
import com.goodee.beedan.repository.order.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j // 💡 로그 출력을 위해 추가
@Service
@RequiredArgsConstructor
public class TrackingService {

    private final ShipmentRepository shipmentRepository;
    private final ObjectMapper objectMapper;

    @Value("${tracker.client.id:}")
    private String clientId;

    @Value("${tracker.client.secret:}")
    private String clientSecret;

    public TrackingResponseDto getTrackingInfo(Long shId) {
        Shipment shipment = shipmentRepository.findById(shId)
                .orElseThrow(() -> new IllegalArgumentException("배송 정보를 찾을 수 없습니다."));

        String carrierId = shipment.getShCarCd();
        String trackingNumber = shipment.getShTraNo();

        RestTemplate restTemplate = new RestTemplate();
        String url = "https://apis.tracker.delivery/graphql";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "TRACKQL-API-KEY " + clientId + ":" + clientSecret);

        String query = "query Track($carrierId: ID!, $trackingNumber: String!) { " +
                "track(carrierId: $carrierId, trackingNumber: $trackingNumber) { " +
                "lastEvent { time status { name } description } " +
                "events(last: 10) { edges { node { time status { name } description } } } " +
                "} }";

        Map<String, Object> variables = Map.of("carrierId", carrierId, "trackingNumber", trackingNumber);
        Map<String, Object> requestBody = Map.of("query", query, "variables", variables);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            return parseGraphQLResponse(response.getBody(), carrierId, trackingNumber);
        } catch (Exception e) {
            log.error("API 통신 실패: {}", e.getMessage());
            return createEmptyResponse(carrierId, trackingNumber, "조회 오류 (서버 통신 실패)");
        }
    }

    private TrackingResponseDto parseGraphQLResponse(String json, String carrierId, String trackingNumber) throws Exception {
        log.info("Delivery Tracker Response: {}", json);

        JsonNode root = objectMapper.readTree(json);
        JsonNode trackNode = root.path("data").path("track");

        if (root.has("errors")) {
            log.error("GraphQL 에러 발생: {}", root.path("errors").toString());
            return createEmptyResponse(carrierId, trackingNumber, "조회 실패 (API 설정 확인)");
        }

        String carrierName = CARRIER_MAP.getOrDefault(carrierId, carrierId);

        if (trackNode.isMissingNode() || trackNode.isNull()) {
            return createEmptyResponse(carrierId, trackingNumber, "배송 정보가 없습니다 (미등록 또는 오입력)");
        }

        List<MemberSummaryDto.TrackingDetailDto> details = new ArrayList<>();
        JsonNode edges = trackNode.path("events").path("edges");

        if (edges.isArray()) {
            for (JsonNode edge : edges) {
                JsonNode node = edge.path("node");

                details.add(MemberSummaryDto.TrackingDetailDto.builder()
                        .time(node.path("time").asText())
                        .status(node.path("status").path("name").asText())
                        .description(node.path("description").asText())
                        .build());
            }
        }

        return TrackingResponseDto.builder()
                .carrierName(carrierName)
                .trackingNumber(trackingNumber)
                .statusText(trackNode.path("lastEvent").path("status").path("name").asText())
                .details(details)
                .build();
    }

    private TrackingResponseDto createEmptyResponse(String carrierId, String trackingNumber, String statusMsg) {
        return TrackingResponseDto.builder()
                .carrierName(carrierId)
                .trackingNumber(trackingNumber)
                .statusText(statusMsg)
                .details(new ArrayList<>())
                .build();
    }

    private static final Map<String, String> CARRIER_MAP = Map.of(
            "kr.cjlogistics", "CJ대한통운",
            "kr.epost", "우체국택배",
            "kr.hanjin", "한진택배",
            "kr.lotteglogis", "롯데택배",
            "kr.logen", "로젠택배",
            "kr.cvsnet", "GS25 편의점택배",
            "kr.cupost", "CU 편의점택배"
    );
}