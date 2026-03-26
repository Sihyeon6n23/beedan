package com.goodee.beedan.notificationTest;

import com.goodee.beedan.common.constant.MemberAuthority;
import com.goodee.beedan.config.security.MemberUserDetails;
import com.goodee.beedan.dto.notification.NotificationDto;
import com.goodee.beedan.entity.Member;
import com.goodee.beedan.repository.notification.NotificationRepository;
import com.goodee.beedan.service.notification.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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

@SpringBootTest
@AutoConfigureMockMvc
public class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private NotificationRepository notificationRepository;

    @MockitoBean
    private NotificationService notificationService;

    private MemberUserDetails mockUser;

    @BeforeEach
    void setUp() {
        Member member = Member.builder()
                .memId(1L)
                .memNm("테스트")
                .memLgnId("user")
                .memLgnPw("1234")
                .memAut(MemberAuthority.USER)
                .memEml("test@mail.com")
                .memCreDt(LocalDateTime.now())
                .build();

        mockUser = new MemberUserDetails(member);
    }

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
    @DisplayName("알림 읽음 처리 성공 테스트")
    @WithMockUser // csrf 토큰 필요함, 사용자 인증 정보 필요함
    void updateNotificationReadStatus() throws Exception {
        Long notificationId = 1L;

        mockMvc.perform(patch("/api/notification/" + notificationId + "/read")
                        .with(csrf()) // CSRF 토큰을 가짜로 생성해서 함께 보냄
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("모든 알림 읽음 처리 및 미확인 목록 반환 테스트")
    void readAllNotifications() throws Exception {
        mockMvc.perform(patch("/api/notification/read-all") // 1. @PutMapping 이므로 put() 사용
                        .with(csrf())                    // 2. 보안을 위한 CSRF 토큰
                        .with(user(mockUser))            // 3. @AuthenticationPrincipal에 들어갈 유저 주입
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())              // 4. ResponseEntity.ok()이므로 200 기대
                .andExpect(jsonPath("$").isArray())      // 5. 반환값이 List<NotificationDto>인지 확인
                .andDo(print());
    }

    @Test
    @DisplayName("특정 알림 삭제 테스트")
    @WithMockUser
    void deleteNotification() throws Exception {
        // given: 삭제할 알림 ID
        Long notificationId = 1L;

        // when & then
        mockMvc.perform(patch("/api/notification/" + notificationId + "/delete")
                        .with(csrf())
                        .with(user(mockUser)))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("모든 알림 삭제 처리 및 미확인 목록 반환 테스트")
    void deleteAllNotifications() throws Exception {
        mockMvc.perform(patch("/api/notification/delete-all") // 1. @PutMapping 이므로 put() 사용
                        .with(csrf())                    // 2. 보안을 위한 CSRF 토큰
                        .with(user(mockUser))            // 3. @AuthenticationPrincipal에 들어갈 유저 주입
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())              // 4. ResponseEntity.ok()이므로 200 기대
                .andExpect(jsonPath("$").isArray())      // 5. 반환값이 List<NotificationDto>인지 확인
                .andDo(print());
    }




}
