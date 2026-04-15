package com.goodee.beedan.repository.crawling;

import com.goodee.beedan.entity.CrawlingUrl;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CrawlingUrlRepository extends JpaRepository<CrawlingUrl, Long> {
    List<CrawlingUrl> findByUrlDelYnFalse();

    List<CrawlingUrl> findByUrlDelYnFalseAndUrlUseYnTrueAndUrlAtYnTrue();

    boolean existsByUrlUrlAndUrlDelYnFalse(String urlUrl);
}
