package com.goodee.beedan.config.security;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("auditorAwareImpl")
public class AuditorAwareImpl implements AuditorAware<Long> {
    @Override
    public Optional<Long> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 2. 로그인을 안 했거나, 익명 사용자라면 빈 값을 반환합니다.
        if (authentication == null || !authentication.isAuthenticated() ||
                authentication.getPrincipal().equals("anonymousUser")) {
            return Optional.of(0L);
        }

        // 3. 앞서 우리가 정성껏 만든 MemberUserDetails로 형변환합니다.
        MemberUserDetails userDetails = (MemberUserDetails) authentication.getPrincipal();

        // 4. 로그인한 회원의 고유 ID(mem_id)를 반환합니다. -> 이게 @LastModifiedBy에 자동으로 꽂힙니다!
        return Optional.of(userDetails.getMemberId());
    }
}
