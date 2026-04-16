package com.goodee.beedan.dto.member;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetDto {
    @NotBlank(message = "비밀번호는 필수 입력입니다.")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{9,}$",
            message = "비밀번호는 9자 이상이며 영문과 숫자 특수기호를 포함해야 합니다."
    )
    private String password;

    private String confirmPassword;

    // 비밀번호 일치 여부 확인 메소드
    public boolean isPasswordMatching() {
        return password != null && password.equals(confirmPassword);
    }

}
