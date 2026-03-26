package com.goodee.beedan.controller.notice;

import com.goodee.beedan.service.notice.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class NoticeApiController {
    private final NoticeService noticeService;

    @GetMapping("/api/notifications/unread-count")
    public ResponseEntity<Integer> getUnreadCount(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) return ResponseEntity.ok(0);

        Long memId = Long.parseLong(userDetails.getUsername());
        return ResponseEntity.ok(noticeService.getUnreadCount(memId));
    }
}
