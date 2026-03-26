package com.goodee.beedan.controller.member;

import com.goodee.beedan.dto.biz.BizDto;
import com.goodee.beedan.service.auth.biz.BizValidateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthRestController {
    private final BizValidateService bizValidateService;
    @PostMapping("/biz-validation")
    public Mono<ResponseEntity<Map<String, Object>>> postBizValidation (
             @RequestBody BizDto bizDto) {
        System.out.println(bizDto);

        return bizValidateService.validate(bizDto)
                .map(resultMap -> ResponseEntity.ok(resultMap))
                .defaultIfEmpty(ResponseEntity.notFound().build());

    }
}
