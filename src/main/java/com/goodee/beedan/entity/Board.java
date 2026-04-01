package com.goodee.beedan.entity;

import com.goodee.beedan.common.constant.BoardType;
import com.goodee.beedan.common.constant.InquiryStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Entity
@Table(name = "board")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Board {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "brd_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "brd_ty", length = 50)
    private BoardType boardType;

    @Column(name = "brd_ttl", length = 100)
    private String title;

    @Lob // LONGTEXT 매핑
    @Column(name = "brd_con")
    private String content;

    // NULL이 들어갈 수 있으므로 원시 타입(long) 대신 Long 사용
    @Column(name = "brd_vst_cnt")
    private Long viewCount;

    // NULL 허용을 위해 Boolean 사용
    @Column(name = "brd_fix_yn")
    private Boolean isFixed;

    @Column(name = "brd_prn_id")
    private Long parentId;

    // DB의 ENUM 타입과 매핑
    @Enumerated(EnumType.STRING)
    @Column(name = "brd_inq_stt")
    private InquiryStatus inquiryStatus;

    @CreatedDate
    @Column(name = "brd_cre_dt")
    private LocalDateTime createdAt;

    @LastModifiedBy
    @Column(name = "brd_upd_mem_id")
    private Long updateMemberId;

    @Column(name = "brd_can_re")
    private String cancelReason;

    @LastModifiedDate
    @Column(name = "brd_upd_dt")
    private LocalDateTime updatedAt;

    // Soft Delete 여부 (NULL 허용)
    @Column(name = "brd_del_yn")
    private Boolean isDeleted;

    // 외래키(FK) 연관관계 매핑을 할 수도 있지만, 식별자만 들고 있는 방식
    @Column(name = "mem_id", nullable = false)
    private Long memberId;

    @PrePersist
    public void prePersist() {
        this.createdAt = this.createdAt == null ? LocalDateTime.now() : this.createdAt;
        this.viewCount = this.viewCount == null ? 0L : this.viewCount;
        this.isDeleted = this.isDeleted != null && this.isDeleted;
        this.isFixed = this.isFixed != null && this.isFixed;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void markAsDeleted() {
        this.isDeleted = true;
    }
}
