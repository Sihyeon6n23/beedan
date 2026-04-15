package com.goodee.beedan.scheduler.exchangeRate;

import com.goodee.beedan.client.exchangeRate.ExchangeRateClient;
import com.goodee.beedan.dto.exchangeRate.ExchangeRateRequest;
import com.goodee.beedan.dto.root.scheduler.SchedulerSettingDto;
import com.goodee.beedan.service.exchangeRate.ExchangeRateService;
import com.goodee.beedan.service.root.SchedulerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExchangeRateScheduler {

    private final ExchangeRateClient client;
    private final ExchangeRateService exchangeRateService;
    private final SchedulerService schedulerService;

    @Scheduled(fixedDelay = 60000)
    public void autoFetchExchangeRate() {
        LocalDateTime now = LocalDateTime.now();
        SchedulerSettingDto setting = schedulerService.getSchedulerSetting();

        // OFF면 리턴
        if (!setting.getIsExchangeRateEnabled()) return;

        // 시작 일자 확인
        if (setting.getExchangeRateStartDt() == null) return;
        LocalDateTime startDt = LocalDateTime.parse(setting.getExchangeRateStartDt());
        if (now.isBefore(startDt)) return;

        // interval 기반 다음 실행 시점 계산
        long hoursElapsed = Duration.between(startDt, now).toHours();
        long intervalHours = Long.parseLong(setting.getExchangeRateInterval());
        LocalDateTime nextRun = startDt.plusHours((hoursElapsed / intervalHours) * intervalHours);

        if (now.isBefore(nextRun)) return;

        // 이미 이번 구간에서 실행했으면 리턴
        if (setting.getLastExchangeRateRunTime() != null) {
            LocalDateTime lastRun = LocalDateTime.parse(setting.getLastExchangeRateRunTime());
            if (!lastRun.isBefore(nextRun)) return;
        }

        // 환율 조회 + 저장
        try {
            log.info("환율 갱신 시작");
            Map<String, Double> rates = client.fetchKrwRates();

            for (Map.Entry<String, Double> entry : rates.entrySet()) {
                ExchangeRateRequest request = ExchangeRateRequest.builder()
                        .erCr(entry.getKey())
                        .erRa(BigDecimal.valueOf(entry.getValue()))
                        .erFDt(now)
                        .build();
                exchangeRateService.create(request);
            }
            log.info("환율 갱신 완료. {}개 통화", rates.size());
        } catch (Exception e) {
            log.error("환율 갱신 실패: {}", e.getMessage(), e);
        }

        // 마지막 실행 시간 저장
        setting.setLastExchangeRateRunTime(LocalDateTime.now().toString());
        schedulerService.saveSchedulerSetting(setting);
    }
}
