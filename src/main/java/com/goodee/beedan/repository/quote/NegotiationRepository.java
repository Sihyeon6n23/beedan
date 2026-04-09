package com.goodee.beedan.repository.quote;

import com.goodee.beedan.entity.Negotiation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NegotiationRepository extends JpaRepository<Negotiation, Long> {

    // 회원별 협상 목록 조회
    @Query("SELECT n FROM Negotiation n WHERE n.memId = :memId ORDER BY n.ngCreDt DESC")
    List<Negotiation> findAllByMemId(@Param("memId") Long memId);

    // 회원별 협상 목록 페이징 조회
    @Query("SELECT n FROM Negotiation n WHERE n.memId = :memId ORDER BY n.ngCreDt DESC")
    Page<Negotiation> findAllByMemId(@Param("memId") Long memId, Pageable pageable);

    // 회원별 진행 중인 협상 조회 (종료일 없음)
    List<Negotiation> findAllByMemIdAndNgEndDtIsNull(Long memId);

    Negotiation findFirstByMemIdOrderByNgCreDtDesc(Long memId);
    Negotiation findByNgId(Long ngId);


}
