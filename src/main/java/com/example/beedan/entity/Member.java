package com.example.beedan.entity;

import com.example.beedan.constant.MemberAuthority;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class Member {
    @Id
    private Long id;
    private Long memId;
    private String memLgnId;
    private String memLgnPw;
    private MemberAuthority memAut;
    private String memNm;

}
