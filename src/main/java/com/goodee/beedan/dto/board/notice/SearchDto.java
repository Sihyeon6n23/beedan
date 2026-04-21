package com.goodee.beedan.dto.board.notice;

import com.goodee.beedan.common.constant.SearchType;
import lombok.Data;
import lombok.Setter;
import lombok.ToString;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.util.List;

@Data
@ToString
public class SearchDto {

    private String keyword;      // 검색어
    private String searchType;

    @Min(value = 0, message = "페이지 번호는 0보다 작을 수 없습니다.")
    private int page = 0;        // 현재 페이지 (Spring Data JPA는 0부터 시작)

    @Min(value = 1, message = "페이지당 최소 1개 이상의 게시글이 필요합니다.")
    @Max(value = 100, message = "한 번에 최대 100개까지만 조회 가능합니다.")
    private int size = 10;       // 페이지당 게시글 수 (보안: 최대치 제한)

    // 문의 게시판 등에서 사용하는 추가 필터 (필요 시)
    private String brdInqStt;
    private Boolean myAnsweredOnly;

    // Getter에서 keyword가 null일 경우 빈 문자열로 반환하여 NullPointerException 방지
    public String getKeyword() {
        return keyword == null ? "" : keyword;
    }
}