package com.goodee.beedan.dto.member;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordChangeDto {
    private String currentPassword;
    private String newPassword;
    private String confirmPassword;

    public boolean isPasswordConfirm() {
        if (this.newPassword == null || this.confirmPassword == null) {
            return false;
        }
        return this.newPassword.equals(this.confirmPassword);
    }

}
