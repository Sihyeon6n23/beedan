package com.goodee.beedan.scheduler.crawling;

import com.goodee.beedan.dto.root.scheduler.SchedulerSettingDto;
import com.goodee.beedan.entity.CrawlingUrl;
import com.goodee.beedan.repository.crawling.CrawlingUrlRepository;
import com.goodee.beedan.service.crawling.CrawlingService;
import com.goodee.beedan.service.root.SchedulerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CrawlingScheduler {

    private final SchedulerService schedulerService;
    private final CrawlingService crawlingService;
    private final CrawlingUrlRepository crawlingUrlRepository;

    @Scheduled(fixedDelay = 60000)
    public void autoCrawl() {
        try {
            LocalDateTime now = LocalDateTime.now();
            // 설정된 세팅값 확인
            SchedulerSettingDto setting = schedulerService.getSchedulerSetting();

            // OFF면 리턴
            if (!setting.getIsAutoCrawlingEnabled()) return;

            // 시작 일시가 설정되지 않았으면 리턴
            if (setting.getAutoCrawlingStartDt() == null || setting.getAutoCrawlingStartDt().isBlank()) return;
            LocalDateTime startDt = LocalDateTime.parse(setting.getAutoCrawlingStartDt());
            if (now.isBefore(startDt)) return;

            // interval 유효성 검증
            if (setting.getAutoCrawlingInterval() == null || setting.getAutoCrawlingInterval().isBlank()) return;
            long intervalHours = Long.parseLong(setting.getAutoCrawlingInterval());
            if (intervalHours <= 0) return;

            // startDt부터 interval 단위로 다음 실행 시점 계산
            long hoursElapsed = Duration.between(startDt, now).toHours();
            LocalDateTime nextRun = startDt.plusHours((hoursElapsed / intervalHours) * intervalHours);

            // 아직 다음 실행 시점이 안 됐으면 리턴
            if (now.isBefore(nextRun)) return;

            // 이미 이번 구간에서 실행했으면 리턴
            if (setting.getLastCrawlingRunTime() != null) {
                LocalDateTime lastRun = LocalDateTime.parse(setting.getLastCrawlingRunTime());
                if (!lastRun.isBefore(nextRun)) return;
            }

            crawlingService.crawlAll();

            // 마지막 시간 업데이트
            setting.setLastCrawlingRunTime(LocalDateTime.now().toString());
            schedulerService.saveSchedulerSetting(setting);
        } catch (Exception e) {
            log.error("자동 크롤링 스케줄러 실행 중 오류 발생", e);
        }
    }
}
