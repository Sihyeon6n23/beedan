package com.goodee.beedan.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CrawlingUrl {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long urlId;
    private String urlUrl;
    private Long brId;
    private Long catId;
}
