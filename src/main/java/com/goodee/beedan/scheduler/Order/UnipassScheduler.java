package com.goodee.beedan.scheduler.Order;

import com.goodee.beedan.common.constant.NotificationType;
import com.goodee.beedan.common.constant.ShipmentStatus;
import com.goodee.beedan.dto.root.scheduler.SchedulerSettingDto;
import com.goodee.beedan.entity.Shipment;
import com.goodee.beedan.repository.order.ShipmentRepository;
import com.goodee.beedan.service.notification.NotificationService;
import com.goodee.beedan.service.order.OrderService;
import com.goodee.beedan.service.order.UnipassService;
import com.goodee.beedan.service.root.SchedulerService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    @Scheduled(fixedDelay = 60000)
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

        runUnipassTracking();
        setting.setLastUnipassRunTime(now.toString());

        try {
            schedulerService.saveSchedulerSetting(setting);
            log.info("UNIPASS 스케줄러 실행 및 설정 저장 완료");
        } catch (IOException e) {
            log.error("UNIPASS 스케줄러 설정 저장 실패: {}", e.getMessage());
        }

    }

    @Transactional
    public void runUnipassTracking() {
        List<Shipment> activeShipments = shipmentRepository.findByShStt(ShipmentStatus.SHIPPING);  // 통관 상태 물품들 확인
        Set<Long> affectedOrderIds = new HashSet<>(); // 중복 체크를 방지하기 위해 이번 사이클에서 영향을 받은 주문 ID들을 저장

        for (Shipment shipment : activeShipments) {
            String hblNo = shipment.getShHblNo();
            String blYear = String.valueOf(shipment.getShCreDt().getYear());

            if (hblNo == null || hblNo.isBlank()) {
                continue;
            }

            String currentStatus = unipassService.getCargoStatus(hblNo, blYear);

            if (currentStatus != null) {
                shipment.setShCusStt(currentStatus);

                if (currentStatus.contains("반출완료")) {
                    shipment.setShStt(ShipmentStatus.DELIVERING);
                }
            }
        }
        shipmentRepository.saveAll(activeShipments);
    }
}