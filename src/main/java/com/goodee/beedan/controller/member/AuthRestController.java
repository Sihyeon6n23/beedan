package com.goodee.beedan.controller.member;

import com.goodee.beedan.dto.member.BizDto;
import com.goodee.beedan.dto.member.sns.MessageResponse;
import com.goodee.beedan.dto.member.sns.SnsDisconnectRequest;
import com.goodee.beedan.dto.member.sns.SnsIntegrateResponse;
import com.goodee.beedan.entity.Member;
import com.goodee.beedan.service.auth.biz.BizValidateService;
import com.goodee.beedan.service.auth.phone.PortOneService;
import com.goodee.beedan.service.member.MemberService;
import com.goodee.beedan.service.member.SnsIntegrateService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthRestController {
    private final BizValidateService bizValidateService;
    private final PortOneService portOneService;
    private final MemberService memberService;
    private final SnsIntegrateService snsIntegrateService;

    @PostMapping("/biz-validation")
    public Mono<ResponseEntity<Map<String, Object>>> postBizValidation (
             @RequestBody BizDto bizDto) {

        return bizValidateService.validate(bizDto)
                .map(resultMap -> ResponseEntity.ok(resultMap))
                .defaultIfEmpty(ResponseEntity.notFound().build());

    }

    // impUid반환함.
    // AuthRestController.java 수정 및 추가

    @PostMapping("/phone-certification")
    public Mono<ResponseEntity<Map<String, Object>>> postPhoneVerification(
            @RequestBody Map<String, String> body) {

        String impUid = body.get("impUid");
        if (impUid == null || impUid.isEmpty()) {
            return Mono.just(ResponseEntity.badRequest().body(Map.of("message", "인증 정보가 없습니다.")));
        }
        Mono<Map<String,Object>> portOneResult = portOneService.verify(impUid);
        // 1. PortOne에서 정보 조회
        return portOneService.MonoToPhoneVerificationDto(portOneResult)
                .map(dto -> {
                    String phoneNumber = dto.getPhoneNumber();

                    // 2. DB 중복 체크 (MemberService 활용)
                    boolean isDuplicated = memberService.isDuplicatedPhoneNumber(phoneNumber);

                    if (isDuplicated) {
                        return ResponseEntity.status(HttpStatus.CONFLICT) // 409 Conflict
                                .body(Map.<String, Object>of(
                                        "success", false,
                                        "message", "이미 가입된 전화번호입니다. 아이디 찾기를 이용해주세요."
                                ));
                    }

                    // 3. 성공 시 응답 (이름과 번호 일부를 내려주어 프론트에서 표시 가능)
                    return ResponseEntity.ok(Map.<String, Object>of(
                            "success", true,
                            "name", dto.getName(),
                            "phone", phoneNumber,
                            "message", "인증에 성공하였습니다."
                    ));
                })
                .onErrorResume(e -> {
                    log.error("본인인증 검증 중 오류 발생: ", e);
                    return Mono.just(ResponseEntity.internalServerError()
                            .body(Map.<String, Object>of(  // <--- 여기 타입을 명시!
                                    "success", false,
                                    "message", "인증 서버와의 통신에 실패했습니다."
                            )));
                });
    }

    @PostMapping("/idChecked")
    public Boolean postIdDuplicateChecked(@RequestBody Map<String, String> body) {
        String username = body.get("username");

        if (username == null || username.trim().isEmpty()) {
            return false;
        }

        boolean isDuplicated = memberService.isDuplicatedLoginId(username);

        return !isDuplicated;
    }

    @PostMapping("/emailChecked")
    public Boolean postEmailDuplicateChecked(@RequestBody Map<String, String> body) {
        String email = body.get("email");

        // 1. 빈 값 검증 (보안 및 오류 방지)
        if (email == null || email.trim().isEmpty()) {
            return false;
        }

        // 2. 서비스 레이어를 통해 이메일 존재 여부 확인
        // 중복된 이메일이 존재하면 true, 없으면 false를 반환한다고 가정
        boolean isDuplicated = memberService.checkEmailDuplicate(email);

        // 3. 프론트엔드 로직에 맞춰 "사용 가능할 때(중복이 아닐 때)" true 반환
        return !isDuplicated;
    }

    @PostMapping("/disconnectSns")
    public Mono<ResponseEntity<MessageResponse>> postDisconnectSns(Principal principal) {
        Member member = memberService.getMemberByUsername(principal.getName());
        SnsDisconnectRequest snsDisconnectRequest = snsIntegrateService.getSnsDisconnectRequest(member);

        return snsIntegrateService.disconnect(snsDisconnectRequest)
                .map(message -> {
                    MessageResponse response =  MessageResponse.builder()
                            .message(message)
                            .redirectPath("/mypage/sns")
                            .build();

                    return ResponseEntity.ok(response);
                });
    }

    @PostMapping("/passwd/reset/request")
    public ResponseEntity<?> postPasswordResetRequest(
            @RequestParam("loginId") String loginId,
            @RequestParam("email") String email) {

        try {
            // 서비스 호출: 일치 여부 확인 후 토큰 생성 및 메일 발송
            memberService.processPasswordReset(loginId, email);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "입력하신 이메일로 비밀번호 재설정 링크를 발송했습니다."
            ));
        } catch (EntityNotFoundException e) {
            // 보안상 "일치하는 정보가 없다"는 메시지를 명확히 주는 것이 좋음
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "일치하는 회원 정보를 찾을 수 없습니다."));
        } catch (Exception e) {
            log.error("비밀번호 재설정 메일 발송 중 오류: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요."));
        }
    }

    @PostMapping("/passwd/reset/validate")
    public ResponseEntity<?> postPasswordResetValidate(
            @RequestParam("loginId") String loginId,
            @RequestParam("email") String email) {
        return null;
    }

    @GetMapping("/find-id")
    public ResponseEntity<?> getIdByEmailAndName(
            @RequestParam("name") String name,
            @RequestParam("email") String email) {

        return memberService.findLoginId(name, email)
                .map(maskedId -> ResponseEntity.ok(Map.of("loginId", maskedId)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "일치하는 회원 정보가 없습니다.")));
    }

    @PostMapping("/check-password")
    public ResponseEntity<Boolean> checkPassword(@RequestBody Map<String, String> payload,
                                                 @AuthenticationPrincipal UserDetails userDetails) {
        String inputPassword = payload.get("currentPassword");
        // 서비스 레이어에서 passwordEncoder.matches()를 사용해 검증
        boolean isValid = memberService.checkCurrentPassword(userDetails.getUsername(), inputPassword);
        return ResponseEntity.ok(isValid);
    }
}
