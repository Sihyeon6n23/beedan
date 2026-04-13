package com.goodee.beedan.controller.tracking;

import com.goodee.beedan.entity.PageView;
import com.goodee.beedan.repository.pageview.PageViewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/tracking")
@RequiredArgsConstructor
public class TrackingApiController {

    private final PageViewRepository pageViewRepository;

    @PostMapping("/pageview")
    public ResponseEntity<Map<String, Object>> recordPageView(
            @RequestBody Map<String, String> body,
            @org.springframework.security.core.annotation.AuthenticationPrincipal
            com.goodee.beedan.config.security.MemberUserDetails userDetails) {
        try {
            String page = body.get("page");
            Long refId = body.get("refId") != null && !body.get("refId").isEmpty() ? Long.parseLong(body.get("refId")) : null;
            Long memId = userDetails != null ? userDetails.getMemberId() : null;

            PageView pv = pageViewRepository.save(PageView.builder().page(page).refId(refId).memId(memId).build());
            return ResponseEntity.ok(Map.of("pvId", pv.getPvId()));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("pvId", 0));
        }
    }

    @PostMapping("/dwell")
    public ResponseEntity<Void> recordDwell(@RequestBody Map<String, Object> body) {
        try {
            Long pvId = body.get("pvId") != null ? Long.parseLong(body.get("pvId").toString()) : null;
            Integer seconds = body.get("seconds") != null ? Integer.parseInt(body.get("seconds").toString()) : null;

            if (pvId != null && seconds != null && seconds > 0) {
                pageViewRepository.findById(pvId).ifPresent(pv -> {
                    pv.setDwellSec(seconds);
                    pageViewRepository.save(pv);
                });
            }
        } catch (Exception ignored) {}
        return ResponseEntity.ok().build();
    }
}
