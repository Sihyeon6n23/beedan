package com.goodee.beedan.controller.tracking;

import com.goodee.beedan.entity.PageView;
import com.goodee.beedan.repository.pageview.PageViewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/tracking")
@RequiredArgsConstructor
public class TrackingApiController {

    private static final int MAX_DWELL_SECONDS = 7200; // 2시간 상한

    private final PageViewRepository pageViewRepository;

    @PostMapping("/pageview")
    public ResponseEntity<Map<String, Object>> recordPageView(
            @RequestBody Map<String, String> body,
            @org.springframework.security.core.annotation.AuthenticationPrincipal
            com.goodee.beedan.config.security.MemberUserDetails userDetails) {
        try {
            String page = body.get("page");
            if (page == null || page.isBlank()) {
                return ResponseEntity.ok(Map.of("pvId", 0));
            }
            Long refId = parseNullableLong(body.get("refId"));
            Long memId = userDetails != null ? userDetails.getMemberId() : null;

            PageView pv = pageViewRepository.save(PageView.builder().page(page).refId(refId).memId(memId).build());
            return ResponseEntity.ok(Map.of("pvId", pv.getPvId()));
        } catch (Exception e) {
            log.warn("PageView 기록 실패: {}", e.getMessage(), e);
            return ResponseEntity.ok(Map.of("pvId", 0));
        }
    }

    @PostMapping("/dwell")
    public ResponseEntity<Void> recordDwell(@RequestBody Map<String, Object> body) {
        try {
            Long pvId = body.get("pvId") != null ? Long.parseLong(body.get("pvId").toString()) : null;
            Integer seconds = body.get("seconds") != null ? Integer.parseInt(body.get("seconds").toString()) : null;

            if (pvId == null || seconds == null || seconds < 1 || seconds > MAX_DWELL_SECONDS) {
                return ResponseEntity.ok().build();
            }

            pageViewRepository.findById(pvId).ifPresent(pv -> {
                pv.setDwellSec(seconds);
                pageViewRepository.save(pv);
            });
        } catch (NumberFormatException e) {
            log.debug("Dwell 파싱 오류: {}", e.getMessage());
        } catch (Exception e) {
            log.warn("Dwell 기록 실패: {}", e.getMessage(), e);
        }
        return ResponseEntity.ok().build();
    }

    private Long parseNullableLong(String value) {
        if (value == null || value.isBlank()) return null;
        try { return Long.parseLong(value); }
        catch (NumberFormatException e) { return null; }
    }
}
