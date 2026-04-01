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
}
