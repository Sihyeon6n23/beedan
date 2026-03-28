package com.goodee.beedan.controller.notification;

import com.goodee.beedan.config.security.MemberUserDetails;
import com.goodee.beedan.dto.notification.NotificationDto;
import com.goodee.beedan.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
public class NotificationApiController {
    private final NotificationService notificationService;

   @GetMapping
    public ResponseEntity<List<NotificationDto>> getNotificationList(@AuthenticationPrincipal MemberUserDetails userDetails) {
       List<NotificationDto> notificationDtoList = notificationService.getUnReadNotificationList(userDetails.getMemberId());

       return ResponseEntity.ok(notificationDtoList);
   }

    @PatchMapping
    public ResponseEntity<List<NotificationDto>> readAll(@AuthenticationPrincipal MemberUserDetails userDetails){
        notificationService.readAll(userDetails.getMemberId());

        List<NotificationDto> notificationDtoList = notificationService.getUnReadNotificationList(userDetails.getMemberId());

        return ResponseEntity.ok(notificationDtoList);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<List<NotificationDto>> read(
            @PathVariable("id") Long notiId,
            @AuthenticationPrincipal MemberUserDetails userDetails){
        notificationService.readNotification(notiId , userDetails.getMemberId());

        List<NotificationDto> notificationDtoList = notificationService.getUnReadNotificationList(userDetails.getMemberId());

        return ResponseEntity.ok(notificationDtoList);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<List<NotificationDto>> delete(
            @PathVariable("id")Long notiId,
            @AuthenticationPrincipal MemberUserDetails userDetails){
        notificationService.deleteNotification(notiId, userDetails.getMemberId());

        List<NotificationDto> notificationDtoList = notificationService.getUnReadNotificationList(userDetails.getMemberId());

        return ResponseEntity.ok(notificationDtoList);
    }

    @DeleteMapping
    public ResponseEntity<List<NotificationDto>> deleteAll(@AuthenticationPrincipal MemberUserDetails userDetails){
        notificationService.deleteAll(userDetails.getMemberId());

        List<NotificationDto> notificationDtoList = notificationService.getUnReadNotificationList(userDetails.getMemberId());

        return ResponseEntity.ok(notificationDtoList);
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Integer> getUnreadCount(@AuthenticationPrincipal MemberUserDetails userDetails) {
        int count = notificationService.getUnreadCount(userDetails.getMemberId());

        return ResponseEntity.ok(count);
    }
}
