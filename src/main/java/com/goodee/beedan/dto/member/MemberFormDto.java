package com.goodee.beedan.dto.member;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberFormDto {
    @Size(min = 4, max = 20, message = "아이디는 4~20자 사이여야 합니다.")
    private String userLoginId;
    @Email(message = "이메일 형식으로 입력해주세요")
    private String email;
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{9,}$",
            message = "비밀번호는 9자 이상이며, 소문자, 숫자, 특수문자를 각각 최소 1개 이상 포함해야 합니다."
    )
    private String password;
    @NotBlank(message = "비밀번호 확인을 입력해주세요.")
    private String confirmPassword;
    @NotBlank(message = "사업자등록번호를 입력해주세요.")
    private String businessRegNum;
    @NotBlank(message = "상호명을 입력해주세요.")
    private String companyName;

    @NotBlank(message = "개업연월일을 입력해주세요.")
    private String establishmentDate;

    @NotBlank(message = "대표자 이름을 입력해주세요.")
    private String ceoName;
    @NotBlank(message = "대표자 연락처를 입력해주세요.")
    private String ceoPhone;
    @NotBlank(message = "회사 대표 연락처를 입력해주세요.")
    private String cmpPhone;

    @NotBlank(message = "주소를 입력해주세요.")
    private String postCode;
    private String companyAddress;
    private String companyAddressDetail;


    // 응답값 확인 필요
    @NotBlank(message = "본인인증을 진행해주세요.")
    private String impUid;

    // 아이디 중복확인 여부 체크
    private Boolean idCheckedInput;
    // 사업자 인증 여부 체크
    private Boolean bizCheckedInput;
    // 본인인증 체크 여부
    private Boolean phoneCheckedInput;

    public boolean isPasswordMatching() {
        if (this.password == null || this.confirmPassword == null) {
            return false;
        }
        return this.password.equals(this.confirmPassword);
    }

}
