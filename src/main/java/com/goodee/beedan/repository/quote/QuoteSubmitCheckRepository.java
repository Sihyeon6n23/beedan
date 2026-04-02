package com.goodee.beedan.repository.quote;

import com.goodee.beedan.entity.QuoteSubmitCheck;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuoteSubmitCheckRepository extends JpaRepository<QuoteSubmitCheck, Long> {

    // 활성화된 항목을 필수/선택 구분하여 정렬 조회
    List<QuoteSubmitCheck> findAllByQscYnTrueOrderByQscRqYnDescQscSortAsc();

    // 활성화된 필수 항목만 정렬 조회
    List<QuoteSubmitCheck> findAllByQscYnTrueAndQscRqYnTrueOrderByQscSortAsc();
}
