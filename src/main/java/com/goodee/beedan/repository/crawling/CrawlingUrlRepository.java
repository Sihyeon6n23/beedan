package com.goodee.beedan.repository.crawling;

import com.goodee.beedan.entity.CrawlingUrl;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CrawlingUrlRepository extends JpaRepository<CrawlingUrl, Long> {
}
