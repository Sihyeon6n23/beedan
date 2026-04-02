package com.goodee.beedan.service.news;

import com.goodee.beedan.client.news.NewsClient;
import com.goodee.beedan.dto.news.NewsResponseDto;
import com.goodee.beedan.service.root.UtilityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NewsService {

    private final NewsClient newsClient;
    private final UtilityService utilityService;

    private static final DateTimeFormatter OUTPUT_FMT = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    public List<Map<String, String>> getLeftNewsList() {
        String query = utilityService.getUtilitySetting().getNewsKeywordLeft();
        return fetchNews(query, "left");
    }

    public List<Map<String, String>> getRightNewsList() {
        String query = utilityService.getUtilitySetting().getNewsKeywordRight();
        return fetchNews(query, "right");
    }

    private List<Map<String, String>> fetchNews(String query, String label) {
        if (query == null || query.isBlank()) {
            log.warn("뉴스 검색어가 설정되지 않았습니다. ({})", label);
            return Collections.emptyList();
        }

        try {
            NewsResponseDto response = newsClient.searchNews(query);
            if (response == null || response.getItems() == null) return Collections.emptyList();

            return Arrays.stream(response.getItems())
                    .limit(4)
                    .map(item -> {
                        Map<String, String> news = new LinkedHashMap<>();
                        news.put("title", stripHtml(item.getTitle()));
                        news.put("pubDate", formatDate(item.getPubDate()));
                        news.put("link", item.getLink());
                        return news;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("뉴스 조회 실패 ({}): {}", label, query, e);
            return Collections.emptyList();
        }
    }

    private String stripHtml(String text) {
        if (text == null) return "";
        return text.replaceAll("<[^>]*>", "").replaceAll("&[a-zA-Z]+;", " ").trim();
    }

    private String formatDate(String pubDate) {
        try {
            ZonedDateTime zdt = ZonedDateTime.parse(pubDate, DateTimeFormatter.RFC_1123_DATE_TIME);
            return zdt.format(OUTPUT_FMT);
        } catch (Exception e) {
            return pubDate != null ? pubDate : "";
        }
    }
}
