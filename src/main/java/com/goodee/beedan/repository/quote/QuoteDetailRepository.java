package com.goodee.beedan.repository.quote;

import com.goodee.beedan.entity.QuoteDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuoteDetailRepository extends JpaRepository<QuoteDetail, Long> {

    // 견적 상세 아이디로 품목 전체 조회
    List<QuoteDetail> findAllByQuInfoId(Long quInfoId);

    // 견적 아이디로 품목 전체 조회
    List<QuoteDetail> findAllByQuId(Long quId);

    // 공장별 품목 조회 (QU_SHIP_FEE 그룹핑 기준)
    List<QuoteDetail> findAllByQuInfoIdAndFaId(Long quInfoId, Long faId);

    // 견적 아이디로 전체 삭제 (임시저장 재저장 시)
    void deleteAllByQuId(Long quId);

    List<QuoteDetail> findAllByNgId(Long ngId);
}