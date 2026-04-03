package com.goodee.beedan.repository.quote;

import com.goodee.beedan.entity.QuoteShipFee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuoteShipFeeRepository extends JpaRepository<QuoteShipFee, Long> {

    // 견적 상세 아이디로 전체 조회 (공장별 배송비 목록)
    List<QuoteShipFee> findAllByQuInfoId(Long quInfoId);

    // 견적 상세 아이디 + 공장 아이디로 조회 (공장별 단건)
    Optional<QuoteShipFee> findByQuInfoIdAndFaId(Long quInfoId, Long faId);

    // 견적 아이디로 전체 조회
    List<QuoteShipFee> findAllByQuId(Long quId);

    // 견적 아이디로 전체 삭제
    void deleteAllByQuId(Long quId);
}