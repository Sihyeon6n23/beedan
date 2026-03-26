package com.goodee.beedan.repository.quote;

import com.goodee.beedan.entity.Negotiation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NegotiationRepository extends JpaRepository<Negotiation, Long> {

    // 회원별 협상 목록 조회
    List<Negotiation> findAllByMemId(Long memId);

    // 회원별 진행 중인 협상 조회 (종료일 없음)
    List<Negotiation> findAllByMemIdAndNgEndDtIsNull(Long memId);



}
