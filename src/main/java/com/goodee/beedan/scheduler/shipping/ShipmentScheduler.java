package com.goodee.beedan.scheduler.shipping;

import com.goodee.beedan.common.constant.ShipmentStatus;
import com.goodee.beedan.dto.order.TrackingResponseDto;
import com.goodee.beedan.dto.root.scheduler.SchedulerSettingDto;
import com.goodee.beedan.entity.Shipment;
import com.goodee.beedan.repository.order.ShipmentRepository;
import com.goodee.beedan.service.order.TrackingService;
import com.goodee.beedan.service.root.SchedulerService;
import com.goodee.beedan.service.shipment.ShipmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShipmentScheduler {
    private final ShipmentRepository shipmentRepository;
    private final ShipmentService shipmentService;
    private final TrackingService trackingService;
    private final SchedulerService schedulerService;

    @Scheduled(fixedDelay = 60000) // 1분마다
    public void trackShipmentSync() {
        LocalDateTime now = LocalDateTime.now();
        SchedulerSettingDto setting = schedulerService.getSchedulerSetting();

        if (!setting.getIsShipmentSyncEnabled()) return;
        if (setting.getShipmentSyncStartDt() == null) return;

        LocalDateTime startDt = LocalDateTime.parse(setting.getShipmentSyncStartDt());
        if (now.isBefore(startDt)) return;

        long hoursElapsed = Duration.between(startDt, now).toHours();
        long intervalHours = Long.parseLong(setting.getShipmentSyncInterval());
        LocalDateTime nextRun = startDt.plusHours((hoursElapsed / intervalHours) * intervalHours);

        if (now.isBefore(nextRun)) return;

        if (setting.getLastShipmentSyncRunTime() != null) {
            LocalDateTime lastRun = LocalDateTime.parse(setting.getLastShipmentSyncRunTime());
            if (!lastRun.isBefore(nextRun)) return;
        }

        syncShipmentStatus();

        setting.setLastShipmentSyncRunTime(now.toString());
        try {
            schedulerService.saveSchedulerSetting(setting);
            log.info("배송 상태 동기화 스케줄러 실행 완료");
        } catch (IOException e) {
            log.error("배송 상태 동기화 스케줄러 설정 저장 실패: {}", e.getMessage());
        }
    }

    public void syncShipmentStatus() {
        try {
            log.info("배송 상태 동기화 스케줄러 시작");

            List<Shipment> targets = shipmentRepository.findByShSttIn(
                    List.of(ShipmentStatus.SHIPPING, ShipmentStatus.DELIVERING)
            );

            int successCount = 0;
            int failCount = 0;

            for (Shipment shipment : targets) {
                try {
                    TrackingResponseDto response = trackingService.getTrackingInfo(shipment.getShId());
                    shipmentService.processSingleShipment(shipment.getShId(), response);
                    successCount++;
                } catch (Exception e) {
                    log.error("송장번호 {} 조회 실패: {}", shipment.getShTraNo(), e.getMessage());
                    failCount++;
                }

                waitForRateLimit();
            }


            log.info("배송 상태 동기화 완료 - 성공: {}, 실패: {}", successCount, failCount);

        } catch (Exception e) {
            log.error("배송 상태 동기화 스케줄러 실행 중 오류 발생", e);
        }
    }

    private void waitForRateLimit() {
        try { Thread.sleep(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}