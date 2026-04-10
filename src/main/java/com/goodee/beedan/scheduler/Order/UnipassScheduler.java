package com.goodee.beedan.scheduler.Order;

import com.goodee.beedan.common.constant.NotificationType;
import com.goodee.beedan.common.constant.ShipmentStatus;
import com.goodee.beedan.dto.root.scheduler.SchedulerSettingDto;
import com.goodee.beedan.entity.Shipment;
import com.goodee.beedan.repository.order.ShipmentRepository;
import com.goodee.beedan.service.notification.NotificationService;
import com.goodee.beedan.service.order.UnipassService;
import com.goodee.beedan.service.root.SchedulerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class UnipassScheduler {
    private final SchedulerService schedulerService;
    private final ShipmentRepository shipmentRepository;
    private final UnipassService unipassService;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelay = 60000000)
    public void trackCustoms() {
        LocalDateTime now = LocalDateTime.now();
        SchedulerSettingDto setting = schedulerService.getSchedulerSetting();

        if (!setting.isUnipassEnabled()) return;

        if (setting.getUnipassStartDt() == null) return;
        LocalDateTime startDt = LocalDateTime.parse(setting.getUnipassStartDt());
        if (now.isBefore(startDt)) return;

        long hoursElapsed = Duration.between(startDt, now).toHours();
        long intervalHours = Long.parseLong(setting.getUnipassInterval());
        LocalDateTime nextRun = startDt.plusHours((hoursElapsed / intervalHours) * intervalHours);

        if (now.isBefore(nextRun)) return;

        if (setting.getLastUnipassRunTime() != null) {
            LocalDateTime lastRun = LocalDateTime.parse(setting.getLastUnipassRunTime());
            if (!lastRun.isBefore(nextRun)) return;
        }

        List<Shipment> activeShipments = shipmentRepository.findByShStt(ShipmentStatus.SHIPPING);  // 통관 상태 물품들 확인

        for (Shipment shipment : activeShipments) {
            String hblNo = shipment.getShHblNo();
            if (hblNo == null || hblNo.isBlank()) continue;

            String blYear = String.valueOf(shipment.getShCreDt().getYear());

            String currentCustomsStatus = unipassService.getCargoStatus(hblNo, blYear); // 상태값을 문자열로 받음

            if (currentCustomsStatus != null && !currentCustomsStatus.isEmpty()) {
                shipment.setShCusStt(currentCustomsStatus);

                if (currentCustomsStatus.contains("물품반출") || currentCustomsStatus.contains("반출확인")) {
                    shipment.setShStt(ShipmentStatus.DELIVERING);
                    log.info("Shipment ID {} : 통관 완료 -> 국내 배송 시작", shipment.getShId());
                }
            }
        }
        
        shipmentRepository.saveAll(activeShipments);
        setting.setLastUnipassRunTime(now.toString());

        try {
            schedulerService.saveSchedulerSetting(setting);
            log.info("UNIPASS 스케줄러 실행 및 설정 저장 완료");
        } catch (IOException e) {
            log.error("UNIPASS 스케줄러 설정 저장 실패: {}", e.getMessage());
        }
    }
}