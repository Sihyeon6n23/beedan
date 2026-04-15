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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShipmentScheduler {
    private final ShipmentRepository shipmentRepository;
    private final ShipmentService shipmentService;
    private final TrackingService trackingService;
    private final SchedulerService schedulerService;

    @Scheduled(cron = "0 0 */3 * * *") // 3시간마다
    public void syncShipmentStatus() {
        try {
            SchedulerSettingDto setting = schedulerService.getSchedulerSetting();

            // 배송 동기화가 비활성화된 경우 실행하지 않음
            if (!setting.getIsShipmentSyncEnabled()) {
                log.info("배송 상태 동기화가 비활성화되어 있어 실행을 건너뜁니다.");
                return;
            }

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

            // 실행 기록 저장
            setting.setLastShipmentSyncRunTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            schedulerService.saveSchedulerSetting(setting);

            log.info("배송 상태 동기화 완료 - 성공: {}, 실패: {}", successCount, failCount);

        } catch (Exception e) {
            log.error("배송 상태 동기화 스케줄러 실행 중 오류 발생", e);
        }
    }

    private void waitForRateLimit() {
        try { Thread.sleep(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}