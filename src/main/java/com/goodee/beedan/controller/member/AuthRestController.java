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
    @PostMapping("/phone-certification")
    public Mono<ResponseEntity<Map<String, Object>>> postPhoneVerification (
            @RequestBody Map<String, String> body) {
        String impUid = body.get("impUid");
        return portOneService.verify(impUid)
                .map(resultMap -> ResponseEntity.ok(resultMap))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping("/idChecked")
    public Boolean postIdDuplicateChecked(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        try {
            memberService.getMemberByUsername(username);
            return false;
        } catch (UsernameNotFoundException e) {
            System.out.println("사용가능항아이디");
            return true;
        }
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
}
