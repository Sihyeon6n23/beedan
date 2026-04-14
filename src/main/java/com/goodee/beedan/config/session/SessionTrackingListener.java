package com.goodee.beedan.config.session;

import com.goodee.beedan.entity.SessionLog;
import com.goodee.beedan.repository.sessionlog.SessionLogRepository;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class SessionTrackingListener implements HttpSessionListener {

    private final SessionLogRepository sessionLogRepository;

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        try {
            String sessionId = se.getSession().getId();
            SessionLog sessionLog = SessionLog.builder()
                    .sessionId(sessionId)
                    .memId(null)
                    .startDt(LocalDateTime.now())
                    .build();
            sessionLogRepository.save(sessionLog);
        } catch (Exception e) {
            log.warn("세션 로그 생성 실패: {}", e.getMessage());
        }
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        try {
            String sessionId = se.getSession().getId();
            sessionLogRepository.findBySlSsId(sessionId).ifPresent(sl -> {
                sl.endSession(LocalDateTime.now());
                sessionLogRepository.save(sl);
            });
        } catch (Exception e) {
            log.warn("세션 로그 종료 처리 실패: {}", e.getMessage());
        }
    }
}
