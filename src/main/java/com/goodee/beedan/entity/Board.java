package com.goodee.beedan.entity;

import com.goodee.beedan.common.constant.BoardType;
import com.goodee.beedan.common.constant.InquiryStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "board")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Board {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "brd_id")
    private Long brdId;

    @Enumerated(EnumType.STRING)
    @Column(name = "brd_ty", length = 50)
    private BoardType brdTy;

    @Column(name = "brd_ttl", length = 100)
    private String brdTtl;

    @Lob // LONGTEXT 매핑
    @Column(name = "brd_con", columnDefinition = "LONGTEXT")
    private String brdCon;

    // NULL이 들어갈 수 있으므로 원시 타입(long) 대신 Long 사용
    @Column(name = "brd_vst_cnt")
    private Long brdVstCnt;

    // NULL 허용을 위해 Boolean 사용
    @Column(name = "brd_fix_yn")
    private Boolean brdFixYn;

    @Column(name = "brd_prn_id")
    private Long brdPrnId;

    // DB의 ENUM 타입과 매핑
    @Enumerated(EnumType.STRING)
    @Column(name = "brd_inq_stt")
    private InquiryStatus brdInqStt;

    @CreatedDate
    @Column(name = "brd_cre_dt")
    private LocalDateTime brdCreDt;

    @LastModifiedBy
    @Column(name = "brd_upd_mem_id")
    private Long brdUpdMemId;

    @Column(name = "brd_can_re")
    private String brdCanRe;

    @LastModifiedDate
    @Column(name = "brd_upd_dt")
    private LocalDateTime brdUpdDt;

    // Soft Delete 여부 (NULL 허용)
    @Column(name = "brd_del_yn")
    private Boolean brdDelYn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mem_id", nullable = false)
    private Member member; // Member 객체 자체를 참조

    @PrePersist
    public void prePersist() {
        this.brdCreDt = this.brdCreDt == null ? LocalDateTime.now() : this.brdCreDt;
        this.brdVstCnt = this.brdVstCnt == null ? 0L : this.brdVstCnt;
        this.brdDelYn = this.brdDelYn != null && this.brdDelYn;
        this.brdFixYn = this.brdFixYn != null && this.brdFixYn;
    }

    // 게시글을 소프트 삭제 상태로 변경
    public void markAsDeleted() {
        this.brdDelYn = true;
    }

    // 게시글 조회수를 1 증가
    public void increaseViewCount() {
        this.brdVstCnt = this.brdVstCnt == null ? 1L : this.brdVstCnt + 1L;
    }

    // 문의글 제목과 내용을 수정
    public void updateInquiry(String title, String content) {
        this.brdTtl = title;
        this.brdCon = content;
    }

    // 답변 내용을 수정
    public void updateAnswer(String content) {
        this.brdCon = content;
    }

    // 문의 상태를 접수로 변경하고 취소 사유를 비움
    public void markReceived() {
        this.brdInqStt = InquiryStatus.RECEIVED;
        this.brdCanRe = null; // 방어 코드
    }

    // 문의 상태를 처리중으로 변경
    public void markInProgress() {
        this.brdInqStt = InquiryStatus.IN_PROGRESS;
    }

    // 문의 상태를 답변 완료로 변경하고 취소 사유를 비움(사용자 취소)
    public void markAnswered() {
        this.brdInqStt = InquiryStatus.ANSWERED;
        this.brdCanRe = null;
    }

    // 문의 상태를 취소로 변경하고 취소 사유를 저장(관리자 취소)
    public void markCancelled(String cancelReason) {
        this.brdInqStt = InquiryStatus.CANCELLED;
        this.brdCanRe = cancelReason;
    }

}
