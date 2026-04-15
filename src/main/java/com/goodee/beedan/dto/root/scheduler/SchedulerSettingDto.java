package com.goodee.beedan.dto.root.scheduler;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SchedulerSettingDto {

    // 1. 크롤링
    // 1.1 자동 크롤링 ON/OFF
    @JsonProperty("is_auto_crawling_enabled")
    private boolean isAutoCrawlingEnabled = false;

    // 1.2 크롤링 시작 일시 (기준 일자)
    @JsonProperty("auto_crawling_start_dt")
    private String autoCrawlingStartDt;

    // 1.3 크롤링 주기 설정
    @JsonProperty("auto_crawling_interval")
    private String autoCrawlingInterval;

    // 1.4 마지막 크롤링 일시
    @JsonProperty("last_crawling_run_time")
    private String lastCrawlingRunTime;


    // 2. 환율
    // 2.1 자동 환율 갱신 ON/OFF
    @JsonProperty("is_exchange_rate_enabled")
    private boolean isExchangeRateEnabled = false;

    // 2.2 환율 갱신 시작 일시
    @JsonProperty("exchange_rate_start_dt")
    private String exchangeRateStartDt;

    // 2.3 환율 갱신 주기 (시간 단위)
    @JsonProperty("exchange_rate_interval")
    private String exchangeRateInterval;

    // 2.4 마지막 환율 갱신 일시
    @JsonProperty("last_exchange_rate_run_time")
    private String lastExchangeRateRunTime;


    // 3. 고객 등급 재산정
    @JsonProperty("is_grade_resolve_enabled")
    private boolean isGradeResolveEnabled = false;

    @JsonProperty("grade_resolve_start_dt")
    private String gradeResolveStartDt;

    @JsonProperty("grade_resolve_interval")
    private String gradeResolveInterval;

    @JsonProperty("last_grade_resolve_run_time")
    private String lastGradeResolveRunTime;


    // 4. 전시용 신규 상품 업데이트 시간 설정
    @JsonProperty("new_stock_update_start_dt")
    private String newStockUpdateStartDt = "2026-04-01T00:00";

    @JsonProperty("new_stock_update_interval")
    private String newStockUpdateInterval = "48";

    @JsonProperty("last_new_stock_update_time")
    private String lastNewStockUpdateTime;


    // 5. 전시용 전월 인기 상품 업데이트 설정 (매월 N일)
    @JsonProperty("popular_stock_update_day")
    private String popularStockUpdateDay = "1";

    @JsonProperty("last_popular_stock_update_time")
    private String lastPopularStockUpdateTime;


    // 6. 채팅방 자동 종료 설정
    // 6.1 ON/OFF
    @JsonProperty("is_chat_auto_close_enabled")
    private boolean isChatAutoCloseEnabled = false;

    // 6.2 시작 시각
    @JsonProperty("chat_auto_close_start_dt")
    private String chatAutoCloseStartDt;

    // 6.3 실행 주기(1시간 마다)
    @JsonProperty("chat_auto_close_interval")
    private String chatAutoCloseInterval;

    // 6.4 마지막 실행 시각
    @JsonProperty("last_chat_auto_close_run_time")
    private String lastChatAutoCloseRunTime;

    // 7. UNIPASS API 호출 (해외 배송/통관 상태 추적)
    @JsonProperty("is_unipass_enabled")
    private Boolean isUnipassEnabled = false;

    @JsonProperty("unipass_start_dt")
    private String unipassStartDt;

    @JsonProperty("unipass_interval")
    private String unipassInterval;

    @JsonProperty("last_unipass_run_time")
    private String lastUnipassRunTime;


    // 8. 배송 상태 동기화
    @JsonProperty("is_shipment_sync_enabled")
    private Boolean isShipmentSyncEnabled = false;

    @JsonProperty("shipment_sync_start_dt")
    private String shipmentSyncStartDt;

    @JsonProperty("shipment_sync_interval")
    private String shipmentSyncInterval;

    @JsonProperty("last_shipment_sync_run_time")
    private String lastShipmentSyncRunTime;


}