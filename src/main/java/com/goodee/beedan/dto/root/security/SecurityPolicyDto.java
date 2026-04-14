package com.goodee.beedan.dto.root.security;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
public class SecurityPolicyDto {

    // 1. 로그인 및 계정 잠금
    // (HTML 폼의 name="isLoginSecurityEnabled"에 맞춰 필드명 수정)
    @JsonProperty("is_login_security_enabled")
    private Boolean isLoginSecurityEnabled = false;

    @JsonProperty("max_login_failure_count")
    private Long maxLoginFailureCount = 0L;

    @JsonProperty("account_lock_duration_minutes")
    private Long accountLockDurationMinutes = 0L;

    // 2. 확장자 제한 필터
    @JsonProperty("is_file_upload_allow_list_enabled")
    private Boolean isFileUploadAllowListEnabled = false;

    @JsonProperty("file_upload_allow_list")
    private Set<String> fileUploadAllowList = new HashSet<>();

    // 3. 세션 타임아웃 (자동 로그아웃)
    @JsonProperty("is_session_timeout_enabled")
    private Boolean isSessionTimeoutEnabled = false;

    @JsonProperty("session_timeout_minutes")
    private Long sessionTimeoutMinutes = 0L;

    // 4. 중복(다중) 로그인 방지
    @JsonProperty("is_concurrent_login_prevented")
    private Boolean isConcurrentLoginPrevented = false;

    // 5. 로그 보관 기간
    @JsonProperty("is_audit_log_enabled")
    private Boolean isAuditLogEnabled = false;

    @JsonProperty("log_retention_days")
    private Long logRetentionDays = 0L;

    // 6. 비밀번호 변경 주기
    @JsonProperty("is_password_expiry_enabled")
    private Boolean isPasswordExpiryEnabled = false;

    @JsonProperty("password_expiry_days")
    private Long passwordExpiryDays = 0L;

}