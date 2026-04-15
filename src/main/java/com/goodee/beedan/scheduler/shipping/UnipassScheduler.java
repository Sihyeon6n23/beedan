package com.goodee.beedan.scheduler.shipping;

import com.goodee.beedan.common.constant.ShipmentStatus;
import com.goodee.beedan.dto.root.scheduler.SchedulerSettingDto;
import com.goodee.beedan.entity.Order;
import com.goodee.beedan.entity.Shipment;
import com.goodee.beedan.repository.order.ShipmentRepository;
import com.goodee.beedan.service.order.UnipassService;
import com.goodee.beedan.service.root.SchedulerService;
import com.goodee.beedan.service.shipment.ShipmentService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;


@Slf4j
@Component
@RequiredArgsConstructor
public class UnipassScheduler {
    private final SchedulerService schedulerService;
    private final ShipmentRepository shipmentRepository;
    private final UnipassService unipassService;
    private final ShipmentService shipmentService;

    @Scheduled(fixedDelay = 60000)
    public void trackCustoms() {
        LocalDateTime now = LocalDateTime.now();
        SchedulerSettingDto setting = schedulerService.getSchedulerSetting();

        if (!setting.getIsUnipassEnabled()) return;
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
        schedulerService.saveSchedulerSetting(setting);
        log.info("UNIPASS 스케줄러 실행 완료");
    }

    @Transactional
    public void runUnipassTracking() {
        List<Shipment> activeShipments = shipmentRepository.findByShSttIn(
                Arrays.asList(ShipmentStatus.PREPARING, ShipmentStatus.SHIPPING)
        );

        // 1. 배송 상태 갱신 및 관련 주문 수집
        Set<Order> ordersToSync = updateShipmentsAndCollectOrders(activeShipments);

        // 2. 주문 상태 일괄 동기화
        syncModifiedOrders(ordersToSync);

    }

    private Set<Order> updateShipmentsAndCollectOrders(List<Shipment> shipments) {
        Set<Order> ordersToSync = new HashSet<>();

        for (Shipment shipment : shipments) {
            if (processSingleShipment(shipment)) {
                Optional.ofNullable(shipment.getOrder()).ifPresent(ordersToSync::add);
            }
            waitForRateLimit();
        }
        return ordersToSync;
    }

    private boolean processSingleShipment(Shipment shipment) {
        String hblNo = shipment.getShHblNo();
        if (hblNo == null || hblNo.isBlank()) return false;

        String blYear = String.valueOf(shipment.getShCreDt().getYear());
        String currentStatus = unipassService.updateCargoStatusForScheduler(hblNo, blYear);

        if (currentStatus == null) return false;

        return updateShipmentState(shipment, currentStatus);
    }

    private boolean updateShipmentState(Shipment shipment, String currentStatus) {
        boolean isChanged = false;
        shipment.setShCusStt(currentStatus);

        if (shipment.getShStt() == ShipmentStatus.PREPARING) {
            shipment.setShStt(ShipmentStatus.SHIPPING);
            isChanged = true;
        }

        if (currentStatus.contains("반출완료") && shipment.getShStt() != ShipmentStatus.DELIVERING) {
            shipment.setShStt(ShipmentStatus.DELIVERING);
            isChanged = true;
        }

        return isChanged;
    }

    private void syncModifiedOrders(Set<Order> orders) {
        orders.forEach(shipmentService::syncOrderStatus);
    }

    private void waitForRateLimit() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            log.warn("스케줄러 딜레이 중 중단됨: {}", e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

}