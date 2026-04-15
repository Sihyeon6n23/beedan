package com.goodee.beedan.scheduler.chat;

import com.goodee.beedan.devUtils.AppDateTime;
import com.goodee.beedan.dto.root.scheduler.SchedulerSettingDto;
import com.goodee.beedan.service.chat.ChatSchedulerService;
import com.goodee.beedan.service.root.SchedulerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatRoomCloseScheduler {
    private final SchedulerService schedulerService;
    private final ChatSchedulerService chatSchedulerService;

    // 1분마다 깨어나지만, 실제 실행 여부는 아래 설정값으로 다시 판단
    @Scheduled(fixedDelay = 60000)
    public void closeInactiveChatRooms() throws IOException {
        // 현재 채팅 자동 종료 스케줄 설정 조회
        SchedulerSettingDto schedulerSetting = schedulerService.getSchedulerSetting();

        // 자동 종료 기능이 꺼져 있으면 바로 종료
        if (!schedulerSetting.getIsChatAutoCloseEnabled()) return;

        // 시작 시각이나 실행 주기가 비어 있으면 안전하게 실행 X
        if (schedulerSetting.getChatAutoCloseStartDt() == null
                || schedulerSetting.getChatAutoCloseInterval() == null) {
            return;
        }

//        log.info("chatAutoClose enabled={}", schedulerSetting.isChatAutoCloseEnabled());
//        log.info("chatAutoClose startDt={}, interval={}, lastRun={}",
//                schedulerSetting.getChatAutoCloseStartDt(),
//                schedulerSetting.getChatAutoCloseInterval(),
//                schedulerSetting.getLastChatAutoCloseRunTime());

        // 현재 시각과 설정된 시작 시각을 비교하기 위해 LocalDateTime으로 변환
        LocalDateTime now = LocalDateTime.now();
        // 테스트용 dev 시간(사용하려면 전역 JPA Auditing 설정을 해야함)
//        LocalDateTime now = AppDateTime.now();
        LocalDateTime start = LocalDateTime.parse(schedulerSetting.getChatAutoCloseStartDt());

        // 아직 시작 시각 전이면 이번 실행은 건너뜀
        if (now.isBefore(start)) {
//            log.info("chatAutoClose skip: now is before start");
            return;
        }


        // 마지막 메시지 이후 3일 지난 OPEN/ONGOING 채팅방을 실제로 자동 종료
        long inactiveHours = Long.parseLong(schedulerSetting.getChatAutoCloseInterval());
        int closedCount = chatSchedulerService.closeInactiveChatRooms(inactiveHours);
        if (closedCount > 0) {
            log.info("비활성 {}시간 초과 채팅방 자동 종료 완료 - 종료된 채팅방 수: {}", inactiveHours, closedCount);
        }

        // 이번 실행 시각을 저장해 다음 실행 주기 계산 기준으로 사용
        schedulerSetting.setLastChatAutoCloseRunTime(now.toString());

        // 갱신된 마지막 실행 시각을 scheduler-setting.json에 반영
        schedulerService.saveSchedulerSetting(schedulerSetting);
    }
}
