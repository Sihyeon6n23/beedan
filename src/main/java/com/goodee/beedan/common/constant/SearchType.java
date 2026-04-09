package com.goodee.beedan.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public enum SearchType {
    ALL("all", "제목+내용", List.of(BoardType.NOTICE, BoardType.INQUIRY, BoardType.FREE)),
    TITLE("title", "제목", List.of(BoardType.NOTICE, BoardType.INQUIRY, BoardType.FREE)),
    WRITER("writer", "작성자", List.of(BoardType.INQUIRY, BoardType.FREE)), // 공지사항은 작성자 검색 제외
    CONTENT("content", "내용", List.of(BoardType.NOTICE, BoardType.INQUIRY, BoardType.FREE)),
    STATUS("status", "처리상태", List.of(BoardType.INQUIRY)); // Q&A 게시판 전용

    private final String value;
    private final String text;
    private final List<BoardType> supportedBoards;

    // 특정 게시판 타입에서 지원하는 검색 조건만 필터링해서 반환
    public static List<SearchType> getSupportedTypes(BoardType type) {
        return Arrays.stream(values())
                .filter(t -> t.supportedBoards.contains(type))
                .collect(Collectors.toList());
    }
}