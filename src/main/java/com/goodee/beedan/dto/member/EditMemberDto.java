package com.goodee.beedan.dto.member;

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
public class EditMemberDto {
    private String memEml;
    private String memCeoPhn;
    private String memCmpTel;
    private String memPosCd;
    private String memBizAdr;
    private String memBizDtAdr;
}
