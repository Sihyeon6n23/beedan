package com.goodee.beedan.controller.member;

import com.goodee.beedan.dto.member.biz.BizDto;
import com.goodee.beedan.service.auth.biz.BizValidateService;
import com.goodee.beedan.service.auth.phone.PortOneService;
import com.goodee.beedan.service.member.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthRestController {
    private final BizValidateService bizValidateService;
    private final PortOneService portOneService;
    private final MemberService memberService;
    @PostMapping("/biz-validation")
    public Mono<ResponseEntity<Map<String, Object>>> postBizValidation (
             @RequestBody BizDto bizDto) {
        System.out.println(bizDto);

        return bizValidateService.validate(bizDto)
                .map(resultMap -> ResponseEntity.ok(resultMap))
                .defaultIfEmpty(ResponseEntity.notFound().build());

    }

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
            memberService.getLoginId(username);
            return false;
        } catch (UsernameNotFoundException e) {
            System.out.println("사용가능항아이디");
            return true;
        }
    }
}
