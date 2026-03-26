package com.goodee.beedan.service.order;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

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

    public void logExtractData(String xml) {
        try {
            JsonNode root = xmlMapper.readTree(xml);

            // 최상위 요약 정보 추출
            JsonNode summary = root.path("cargCsclPrgsInfoQryVo");
            log.info("================ [화물 요약 정보] ================");
            log.info("운송장 번호: {}", summary.path("hblNo").asText());
            log.info("현재 상태: {}", summary.path("prgsStts").asText());
            log.info("품명: {}", summary.path("prnm").asText());

            JsonNode details = root.path("cargCsclPrgsInfoDtlQryVo");
            log.info("================ [진행 이력 리스트] ================");

            if (details.isArray()) {
                details.forEach(detail -> printDetail(detail));
            } else if (!details.isMissingNode()) {
                printDetail(details);
            }
        } catch (Exception e) {
            log.error("데이터 추출 중 오류: {}", e.getMessage());
        }
    }

    private void printDetail(JsonNode detail) {
        log.info("[{}] {} - {}",
                detail.path("prcsDttm").asText(),
                detail.path("cargTrcnRelaBsopTpcd").asText(),
                detail.path("shedNm").asText());
    }
}