package com.goodee.beedan.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tkId;
    private String tkVl;
    private String tkTy;
    private LocalDateTime tkExpDt;
    private Boolean tkUseYn;
    private LocalDateTime tkCreDt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mem_id")
    private Member member;

    public void useToken() {
        this.tkUseYn = true;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.tkExpDt) || Boolean.TRUE.equals(this.tkUseYn);
    }
}