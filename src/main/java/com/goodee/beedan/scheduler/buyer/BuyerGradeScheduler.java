package com.goodee.beedan.scheduler.buyer;

import com.goodee.beedan.dto.root.scheduler.SchedulerSettingDto;
import com.goodee.beedan.service.buyer.BuyerService;
import com.goodee.beedan.service.root.SchedulerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class BuyerGradeScheduler {

    private final BuyerService buyerService;
    private final SchedulerService schedulerService;

    @Scheduled(fixedDelay = 60000)
    public void autoResolveGrade() {
        LocalDateTime now = LocalDateTime.now();
        SchedulerSettingDto setting = schedulerService.getSchedulerSetting();

        if (!setting.isGradeResolveEnabled()) return;

        if (setting.getGradeResolveStartDt() == null) return;
        LocalDateTime startDt = LocalDateTime.parse(setting.getGradeResolveStartDt());
        if (now.isBefore(startDt)) return;

        long hoursElapsed = Duration.between(startDt, now).toHours();
        long intervalHours = Long.parseLong(setting.getGradeResolveInterval());
        LocalDateTime nextRun = startDt.plusHours((hoursElapsed / intervalHours) * intervalHours);

        if (now.isBefore(nextRun)) return;

        if (setting.getLastGradeResolveRunTime() != null) {
            LocalDateTime lastRun = LocalDateTime.parse(setting.getLastGradeResolveRunTime());
            if (!lastRun.isBefore(nextRun)) return;
        }

        try {
            log.info("고객 등급 재산정 시작");
            buyerService.bulkResolveGrade();
            log.info("고객 등급 재산정 완료");
        } catch (Exception e) {
            log.error("고객 등급 재산정 실패: {}", e.getMessage(), e);
        }

        setting.setLastGradeResolveRunTime(LocalDateTime.now().toString());
        try {
            schedulerService.saveSchedulerSetting(setting);
        } catch (IOException e) {
            log.error("스케줄러 설정 저장 실패", e);
        }
    }
}
