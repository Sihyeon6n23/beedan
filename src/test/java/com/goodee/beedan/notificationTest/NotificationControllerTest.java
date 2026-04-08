package com.goodee.beedan.notificationTest;

import com.goodee.beedan.common.constant.MemberAuthority;
import com.goodee.beedan.config.security.MemberUserDetails;
import com.goodee.beedan.controller.notification.NotificationApiController;
import com.goodee.beedan.dto.notification.NotificationDto;
import com.goodee.beedan.entity.Member;
import com.goodee.beedan.repository.notification.NotificationRepository;
import com.goodee.beedan.service.notification.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@WebMvcTest(NotificationApiController.class)
public class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private NotificationRepository notificationRepository;

    @MockitoBean
    private NotificationService notificationService;

    private MemberUserDetails mockUser;

//    @BeforeEach
//    void setUp() {
//        Member member = Member.builder()
//                .memId(1L)
//                .memNm("테스트")
//                .memLgnId("user")
//                .memLgnPw("1234")
//                .memAut(MemberAuthority.USER)
//                .memEml("test@mail.com")
//                .memCreDt(LocalDateTime.now())
//                .build();
//
//        mockUser = new MemberUserDetails(member);
//    }

    @Test
    void debugTest() {
        System.out.println("전체 알림 수: " + notificationRepository.findAll().size());
        System.out.println("1번 유저의 알림 수: " + notificationRepository.countByMember_MemIdAndNotiReaYnFalseAndNotiDelYnFalse(1L));
    }

    @Test
    @DisplayName("알림 목록 조회 (GET /api/notification/list)")
    void getNotificationListTest() throws Exception {
        List<NotificationDto> mockList = List.of(new NotificationDto(), new NotificationDto());
        given(notificationService.getUnReadNotificationList(any())).willReturn(mockList);

        mockMvc.perform(get("/api/notification/list")
                        .with(user(mockUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2)); // 리스트 크기가 2인지 확인
    }

    @Test
    @DisplayName("알림 읽음 처리 성공 시 미확인 알림 목록 반환 테스트")
    @WithMockUser
    void updateNotificationReadStatus() throws Exception {
        Long notificationId = 1L;

        NotificationDto mockDto = new NotificationDto();

        given(notificationService.getUnReadNotificationList(any())).willReturn(List.of(mockDto));

        mockMvc.perform(patch("/api/notification/" + notificationId + "/read")
                        .with(user(mockUser))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0]").exists()) // DTO 필드 검증 전 객체가 있는지 확인
                .andDo(print());
    }

    @Test
    @DisplayName("모든 알림 읽음 처리 및 미확인 목록 반환 테스트")
    void readAllNotifications() throws Exception {
        // given: 모두 읽음 처리했으므로, 남은 '미확인' 알림 목록 조회 시 빈 배열([])을 반환하도록 대본 설정
        given(notificationService.getUnReadNotificationList(any())).willReturn(List.of());

        // when & then
        mockMvc.perform(patch("/api/notification/read-all")
                        .with(csrf())
                        .with(user(mockUser))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0)) // 배열 크기가 0인지 완벽 검증!
                .andDo(print());
    }

    @Test
    @DisplayName("특정 알림 삭제 테스트")
    @WithMockUser
    void deleteNotification() throws Exception {
        // given: 삭제할 알림 ID 지정
        Long notificationId = 1L;

        // 특정 알림 1개를 삭제한 후, 아직 남은 다른 알림(MockDto)이 1개 있다고 가짜 설정
        NotificationDto remainingDto = new NotificationDto();
        given(notificationService.getUnReadNotificationList(any())).willReturn(List.of(remainingDto));

        // when & then
        mockMvc.perform(patch("/api/notification/" + notificationId + "/delete")
                        .with(csrf())
                        .with(user(mockUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1)) // 남은 알림이 1개인지 검증
                .andDo(print());
    }

    @Test
    @DisplayName("모든 알림 삭제 처리 및 미확인 목록 반환 테스트")
    void deleteAllNotifications() throws Exception {
        // given: 모두 삭제 처리했으므로, 남은 알림 목록 조회 시 빈 배열([])을 반환하도록 설정
        given(notificationService.getUnReadNotificationList(any())).willReturn(List.of());

        // when & then
        mockMvc.perform(patch("/api/notification/delete-all")
                        .with(csrf())
                        .with(user(mockUser))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0)) // 배열 크기가 0인지 검증
                .andDo(print());
    }



}
