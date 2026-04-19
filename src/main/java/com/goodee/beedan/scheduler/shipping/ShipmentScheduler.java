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
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

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

    private final CacheManager cacheManager; // 캐시 설정을 위함

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
        schedulerService.saveSchedulerSetting(setting);
    }

    public void syncShipmentStatus() {
        log.info("배송 상태 동기화 스케줄러 시작");

        List<Shipment> targets = shipmentRepository.findByShSttIn(
                List.of(ShipmentStatus.SHIPPING, ShipmentStatus.DELIVERING)
        );

        Cache deliveryCache = cacheManager.getCache("shipment:delivery");  // Redis 캐시 매니저를 통해 해당 캐시 영역을 가져옴

        for (Shipment shipment : targets) {
            try {
                if (deliveryCache != null)  deliveryCache.evict(shipment.getShId());

                // TrackingService.getTrackingInfo 내부에 @Cacheable이 필요함
                TrackingResponseDto response = trackingService.getTrackingInfo(shipment.getShId());
                shipmentService.processSingleShipment(shipment.getShId(), response);

            } catch (Exception e) {
                log.error("배송 ID {} 조회 실패: {}", shipment.getShId(), e.getMessage());
            }
            waitForRateLimit();
        }
    }

    private void waitForRateLimit() {
        try { Thread.sleep(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}