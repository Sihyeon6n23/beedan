package com.goodee.beedan.dto.root.security;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class SecurityPolicyDto {

    // 1. 로그인 실패 횟수 제한
    @JsonProperty("is_login_failure_limit_enabled")
    private Boolean isLoginFailureLimitEnabled = false;

    @JsonProperty("max_login_failure_count")
    private Long maxLoginFailureCount = 0L; // 0으로 초기화

    // 2. 계정 잠금 시간
    @JsonProperty("is_account_lock_enabled")
    private Boolean isAccountLockEnabled = false;

    @JsonProperty("account_lock_duration_minutes")
    private Long accountLockDurationMinutes = 0L; // 0으로 초기화

    // 3. 세션 타임아웃 (자동 로그아웃)
    @JsonProperty("is_session_timeout_enabled")
    private Boolean isSessionTimeoutEnabled = false;

    @JsonProperty("session_timeout_minutes")
    private Long sessionTimeoutMinutes = 0L; // 0으로 초기화

    // 4. 파일 업로드 허용 확장자
    @JsonProperty("is_file_upload_allow_list_enabled")
    private Boolean isFileUploadAllowListEnabled = false;

    @JsonProperty("file_upload_allow_list")
    private Set<String> fileUploadAllowList = Collections.emptySet();

    // 5. 로그 보관 기간
    @JsonProperty("is_audit_log_enabled")
    private Boolean isAuditLogEnabled = false;

    @JsonProperty("log_retention_days")
    private Long logRetentionDays = 0L; // 0으로 초기화

    // 6. 중복(다중) 로그인 방지
    @JsonProperty("is_concurrent_login_prevented")
    private Boolean isConcurrentLoginPrevented = false;

    // 7. 비밀번호 변경 주기
    @JsonProperty("is_password_expiry_enabled")
    private Boolean isPasswordExpiryEnabled = false;

    @JsonProperty("password_expiry_days")
    private Long passwordExpiryDays = 0L; // 0으로 초기화

    private Set<String> fileUploadAllowSet;

    public void setFileUploadAllowList(Object value) {
        if (value instanceof String str) {
            this.fileUploadAllowList = Arrays.stream(str.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toSet());
        }
    }

    public String getFileUploadAllowList() {
        return String.join(", ", this.fileUploadAllowList);
    }
}