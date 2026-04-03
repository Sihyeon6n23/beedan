package com.goodee.beedan.dto.news;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class NewsResponseDto {
    private String lastBuildDate;
    private int total;
    private int start;
    private int display;
    private Item[] items;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Item {
        private String title;
        private String link;
        private String description;
        private String pubDate;
    }
}
