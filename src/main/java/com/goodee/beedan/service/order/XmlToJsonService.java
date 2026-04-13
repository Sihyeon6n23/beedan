package com.goodee.beedan.service.order;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.goodee.beedan.dto.order.TrackingResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class XmlToJsonService {

    private final ObjectMapper jsonMapper = new ObjectMapper();
    private final XmlMapper xmlMapper = new XmlMapper();

    public String convertXmlToJson(String xml) {
        try {
            JsonNode node = xmlMapper.readTree(xml.getBytes(StandardCharsets.UTF_8)); // JsonNode: XML 문자열을 읽어 트리 구조로 변환

            return jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(node); // JSON 문자열로 변환
        } catch (Exception e) {
            log.error("JSON 변환 중 오류 발생: {}", e.getMessage());
            return null;
        }
    }

    public String extractProgressStatus(String xml) {
        if (xml == null || xml.isBlank()) return null;

        try {
            JsonNode root = xmlMapper.readTree(xml.getBytes(StandardCharsets.UTF_8));

            JsonNode tCntNode = root.path("tCnt");
            if (!tCntNode.isMissingNode() && tCntNode.asInt() == 0) {
                log.debug("조회된 통관 정보가 없습니다 (tCnt=0).");
                return null;
            }

            JsonNode summary = root.path("cargCsclPrgsInfoQryVo");
            return summary.path("prgsStts").asText(null);

        } catch (Exception e) {
            log.error("XML에서 상태값 추출 중 오류 발생: {}", e.getMessage());
            return null;
        }
    }

    public List<TrackingResponseDto.TrackingDetail> extractCustomsTimeline(String xml) {
        List<TrackingResponseDto.TrackingDetail> details = new ArrayList<>();
        if (xml == null || xml.isBlank()) return details;

        try {
            JsonNode root = xmlMapper.readTree(xml.getBytes(StandardCharsets.UTF_8));

            JsonNode tCntNode = root.path("tCnt");  // tCnt가 0이면 불필요한 파싱 없이 빈 리스트 반환
            if (!tCntNode.isMissingNode() && tCntNode.asInt() == 0) { return details;}

            // UNIPASS의 상세 진행 이력 노드
            JsonNode historyNodes = root.path("cargCsclPrgsInfoDtlQryVo");

            if (historyNodes.isArray()) {
                for (JsonNode node : historyNodes) {
                    details.add(TrackingResponseDto.TrackingDetail.builder()
                            .time(node.path("prcsDttm").asText())
                            .status(node.path("cargTrcnRelaBsopTpcd").asText())
                            .description(node.path("shedNm").asText())
                            .build());
                }
            } else if (!historyNodes.isMissingNode()) { // 이력이 하나만 있을 경우 배열이 아니라 객체로 올 수 있으므로 예외 처리
                details.add(TrackingResponseDto.TrackingDetail.builder()
                        .time(historyNodes.path("prcsDttm").asText())
                        .status(historyNodes.path("cargTrcnRelaBsopTpcd").asText())
                        .description(historyNodes.path("shedNm").asText())
                        .build());
            }
        } catch (Exception e) {
            log.error("통관 이력 파싱 중 오류: {}", e.getMessage());
        }
        return details;
    }

}