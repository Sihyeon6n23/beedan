package com.goodee.beedan.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "chatbot_response")
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatbotResponse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cb_res_id")
    private Long cbResId;

    @Column(name = "cb_res_ttl", nullable = false, length = 255)
    private String cbResTtl;

    @Lob
    @Column(name = "cb_res_con", nullable = false)
    private String cbResCon;

    @Column(name = "cb_res_lnk_btn_nm", length = 100)
    private String cbResLnkBtnNm;

    @Column(name = "cb_res_lnk_url", length = 255)
    private String cbResLnkUrl;

    // 링크 버튼은 버튼명과 URL이 함께 있거나 함께 없어야 한다.
    @Column(name = "cb_res_use_yn", nullable = false)
    private boolean cbResUseYn;

    @CreatedDate
    @Column(name = "cb_res_cre_dt", nullable = false, updatable = false)
    private LocalDateTime cbResCreDt;

    @LastModifiedDate
    @Column(name = "cb_res_upd_dt", nullable = false)
    private LocalDateTime cbResUpdDt;

    @Column(name = "cb_tp_id", nullable = false, unique = true)
    private Long cbTpId;
}
