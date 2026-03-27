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

   @GetMapping("/list")
    public ResponseEntity<List<NotificationDto>> getNotificationList(@AuthenticationPrincipal MemberUserDetails userDetails) {
       List<NotificationDto> notificationDtoList = notificationService.getUnReadNotificationList(userDetails.getMemberId());

       return ResponseEntity.ok(notificationDtoList);
   }

    @PatchMapping("/{id}/read")
    public ResponseEntity<List<NotificationDto>> read(
            @PathVariable("id") Long notiId,
            @AuthenticationPrincipal MemberUserDetails userDetails){
        notificationService.readNotification(notiId);

        List<NotificationDto> notificationDtoList = notificationService.getUnReadNotificationList(userDetails.getMemberId());

        return ResponseEntity.ok(notificationDtoList);
    }

    @PatchMapping("/read-all")
    public ResponseEntity<List<NotificationDto>> readAll(@AuthenticationPrincipal MemberUserDetails userDetails){
        notificationService.readAll(userDetails.getMemberId());

        List<NotificationDto> notificationDtoList = notificationService.getUnReadNotificationList(userDetails.getMemberId());

        return ResponseEntity.ok(notificationDtoList);
    }

    @PatchMapping("/{id}/delete")
    public ResponseEntity<List<NotificationDto>> delete(
            @PathVariable("id")Long notiId,
            @AuthenticationPrincipal MemberUserDetails userDetails){
        notificationService.deleteNotification(notiId);

        List<NotificationDto> notificationDtoList = notificationService.getUnReadNotificationList(userDetails.getMemberId());

        return ResponseEntity.ok(notificationDtoList);
    }

    @PatchMapping("/delete-all")
    public ResponseEntity<List<NotificationDto>> deleteAll(@AuthenticationPrincipal MemberUserDetails userDetails){
        notificationService.deleteAll(userDetails.getMemberId());

        List<NotificationDto> notificationDtoList = notificationService.getUnReadNotificationList(userDetails.getMemberId());

        return ResponseEntity.ok(notificationDtoList);
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Integer> getUnreadCount(@AuthenticationPrincipal MemberUserDetails userDetails) {
        if (userDetails == null) {
            System.out.println("디버깅: userDetails가 null입니다!");
            return ResponseEntity.ok(0);
        }

        int count = notificationService.getUnreadCount(userDetails.getMemberId());

        System.out.println("디버깅: 요청온 memId = " + userDetails.getMemberId());
        System.out.println("디버깅: 조회된 알림 개수 = " + count);

        return ResponseEntity.ok(count);
    }
}
