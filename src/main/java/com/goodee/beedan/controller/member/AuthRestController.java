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
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
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
}
