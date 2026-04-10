package com.goodee.beedan.dto.member;

import com.goodee.beedan.common.constant.MemberAuthority;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberCreateFormDto {

    @Size(min = 4, max = 20, message = "아이디는 4~20자 사이여야 합니다.")
    @NotBlank(message = "아이디를 입력해주세요.")
    private String userLoginId;

    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{9,}$",
            message = "비밀번호는 9자 이상이며, 소문자, 숫자, 특수문자를 각각 최소 1개 이상 포함해야 합니다."
    )
    private String password;

    @NotBlank(message = "비밀번호 확인을 입력해주세요.")
    private String confirmPassword;

    @NotBlank(message = "성함을 입력해주세요.")
    private String name;

    @Email(message = "이메일 형식으로 입력해주세요")
    @NotBlank(message = "이메일을 입력해주세요.")
    private String email;

    @NotBlank(message = "연락처를 입력해주세요.")
    private String phone;

    // 관리자 권한 설정 (기본값 ADMIN)
    private MemberAuthority authority;

    // 아이디 중복확인 여부 체크용
    private Boolean idCheckedInput = false;

    public boolean isPasswordMatching() {
        return this.password != null && this.password.equals(this.confirmPassword);
    }
}