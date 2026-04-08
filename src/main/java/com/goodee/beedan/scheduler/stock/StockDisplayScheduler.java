package com.goodee.beedan.scheduler.stock;

import com.goodee.beedan.dto.root.scheduler.SchedulerSettingDto;
import com.goodee.beedan.service.root.SchedulerService;
import com.goodee.beedan.service.stock.StockDisplayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockDisplayScheduler {

    private final SchedulerService schedulerService;
    private final StockDisplayService stockDisplayService;

    @Scheduled(fixedDelay = 60000)
    public void updateNewStocks() {
        LocalDateTime now = LocalDateTime.now();
        SchedulerSettingDto setting = schedulerService.getSchedulerSetting();

        LocalDateTime startDt = LocalDateTime.parse(setting.getNewStockUpdateStartDt());
        if (now.isBefore(startDt)) return;

        long hoursElapsed = Duration.between(startDt, now).toHours();
        long intervalHours = Long.parseLong(setting.getNewStockUpdateInterval());
        LocalDateTime nextRun = startDt.plusHours((hoursElapsed / intervalHours) * intervalHours);

        if (now.isBefore(nextRun)) return;

        if (setting.getLastNewStockUpdateTime() != null) {
            LocalDateTime lastRun = LocalDateTime.parse(setting.getLastNewStockUpdateTime());
            if (!lastRun.isBefore(nextRun)) return;
        }

        stockDisplayService.refreshNewStocks();

        setting.setLastNewStockUpdateTime(LocalDateTime.now().toString());
        try {
            schedulerService.saveSchedulerSetting(setting);
        } catch (IOException e) {
            log.error("스케줄러 설정 저장 실패", e);
        }
    }

    @Scheduled(fixedDelay = 60000)
    public void updatePopularStocks() {
        LocalDate today = LocalDate.now();
        SchedulerSettingDto setting = schedulerService.getSchedulerSetting();

        // 오늘이 설정된 날짜가 아니면 리턴
        int updateDay = Integer.parseInt(setting.getPopularStockUpdateDay());
        if (today.getDayOfMonth() != updateDay) return;

        // 이번 달에 이미 실행했으면 리턴
        if (setting.getLastPopularStockUpdateTime() != null) {
            LocalDate lastRunDate = LocalDateTime.parse(setting.getLastPopularStockUpdateTime()).toLocalDate();
            if (lastRunDate.getMonthValue() == today.getMonthValue()
                    && lastRunDate.getYear() == today.getYear()) return;
        }

        stockDisplayService.refreshPopularStocks();

        setting.setLastPopularStockUpdateTime(LocalDateTime.now().toString());
        try {
            schedulerService.saveSchedulerSetting(setting);
        } catch (IOException e) {
            log.error("스케줄러 설정 저장 실패", e);
        }
    }
}
