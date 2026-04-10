package com.goodee.beedan.service.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.goodee.beedan.common.constant.ShipmentStatus;
import com.goodee.beedan.dto.admin.MemberSummaryDto;
import com.goodee.beedan.dto.order.TrackingDetailDto;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrackingService {
    private final ShipmentRepository shipmentRepository;
    private final ObjectMapper objectMapper;

    private final UnipassService unipassService;

    @Value("${tracker.client.id:}")
    private String clientId;

    @Value("${tracker.client.secret:}")
    private String clientSecret;

    public TrackingResponseDto getTrackingInfo(Long shId) {
        Shipment shipment = shipmentRepository.findById(shId).orElseThrow(() -> new IllegalArgumentException("배송 정보를 찾을 수 없습니다."));

        List<TrackingResponseDto.TrackingDetail> customsDetails = new ArrayList<>();
        if (shipment.getShHblNo() != null) {
            String blYear = String.valueOf(shipment.getShCreDt().getYear());
            customsDetails = unipassService.getCustomsTimeline(shipment.getShHblNo(), blYear);
        }

        String carrierId = shipment.getShCarCd();
        String trackingNumber = shipment.getShTraNo();

        if (trackingNumber.startsWith("TEST-")) return generateMockTrackingResponse(shipment);


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
            return parseGraphQLResponse(response.getBody(), carrierId, trackingNumber, shipment, customsDetails);
        } catch (Exception e) {
            log.error("API 통신 실패: {}", e.getMessage());
            TrackingResponseDto errorResponse = createEmptyResponse(carrierId, trackingNumber, "조회 오류");   // 에러 발생 시에도 통관 정보는 보여줄 수 있도록 처리
            errorResponse.setCustomsDetails(customsDetails);
            return createEmptyResponse(carrierId, trackingNumber, "조회 오류 (서버 통신 실패)");
        }
    }

    private TrackingResponseDto parseGraphQLResponse(String json,
                                                     String carrierId,
                                                     String trackingNumber,
                                                     Shipment shipment,
                                                     List<TrackingResponseDto.TrackingDetail> customsDetails) throws Exception {
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

        List<TrackingDetailDto> details = new ArrayList<>();
        JsonNode edges = trackNode.path("events").path("edges");

        if (edges.isArray()) {
            for (JsonNode edge : edges) {
                JsonNode node = edge.path("node");

                details.add(TrackingDetailDto.builder()
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
                .customsDetails(customsDetails) // 해외 통관 (추가)
                .shRcvNm(shipment.getShRcvNm())
                .shAdr(shipment.getShAdr())
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

    private TrackingResponseDto generateMockTrackingResponse(Shipment shipment) {
        List<TrackingDetailDto> details = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        ShipmentStatus status = shipment.getShStt();

        details.add(new TrackingDetailDto(now.minusDays(1).toString(), "상품준비", "판매자가 상품을 발송하기 위해 준비 중입니다."));

        if (status != ShipmentStatus.PREPARING) details.add(new TrackingDetailDto(now.minusHours(5).toString(), "통관처리", "세관 검사가 진행 중입니다."));

        if (status == ShipmentStatus.DELIVERING || status == ShipmentStatus.DELIVERED) {
            details.add(new TrackingDetailDto(
                    now.minusHours(2).toString(), "배송중", "고객님의 지역으로 물건이 이동 중입니다."));
        }

        if (status == ShipmentStatus.DELIVERED) {
            details.add(new TrackingDetailDto(
                    now.toString(), "배송완료", "배송이 완료되었습니다."));
        }

        return TrackingResponseDto.builder()
                .carrierName("시연용 가상택배")
                .trackingNumber(shipment.getShTraNo())
                .statusText(shipment.getShStt().getStatusName())
                .details(details)
                .shAdr(shipment.getShAdr())
                .shRcvNm(shipment.getShRcvNm())
                .build();
    }

}