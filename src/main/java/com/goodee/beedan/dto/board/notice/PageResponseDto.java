package com.goodee.beedan.dto.board.notice;

import lombok.Getter;
import org.springframework.data.domain.Page;
import java.util.List;

@Getter
public class PageResponseDto<T> {
    private final List<T> content;
    private final int page;
    private final int size;
    private final long totalElements;
    private final int totalPages;
    private final int startPage;
    private final int endPage;
    private final boolean hasPrev;
    private final boolean hasNext;

    public PageResponseDto(Page<T> pageObj, int blockLimit) {
        this.content = pageObj.getContent();
        this.page = pageObj.getNumber();
        this.size = pageObj.getSize();
        this.totalElements = pageObj.getTotalElements();
        this.totalPages = pageObj.getTotalPages();

        // 계산 로직 (기존 유지)
        int calculatedStart = Math.max(0, (this.page / blockLimit) * blockLimit);
        int calculatedEnd = Math.min(calculatedStart + blockLimit - 1, this.totalPages - 1);

        this.startPage = calculatedStart;
        this.endPage = Math.max(0, calculatedEnd);

        this.hasPrev = pageObj.hasPrevious();
        this.hasNext = pageObj.hasNext();
    }

    // Thymeleaf 호환성을 위한 Getter 명시
    public boolean hasPrevious() {
        return this.hasPrev;
    }

    public boolean hasNext() {
        return this.hasNext;
    }
}