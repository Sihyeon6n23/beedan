package com.goodee.beedan.controller.notification;

import com.goodee.beedan.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class NotificationApiController {
    private final NotificationService notificationService;

    @GetMapping("/api/notifications/unread-count")
    public ResponseEntity<Integer> getUnreadCount(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) return ResponseEntity.ok(0);

        Long memId = Long.parseLong(userDetails.getUsername());
        return ResponseEntity.ok(notificationService.getUnreadCount(memId));
    }
}
