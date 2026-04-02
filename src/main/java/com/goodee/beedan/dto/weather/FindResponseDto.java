package com.goodee.beedan.dto.weather;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FindResponseDto {
    private int count;
    private List<Item> list;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Item {
        private String name;
        private Sys sys;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Sys {
        private String country;
    }
}
