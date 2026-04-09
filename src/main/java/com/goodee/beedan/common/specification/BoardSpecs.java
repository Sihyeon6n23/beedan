package com.goodee.beedan.common.specification;

import com.goodee.beedan.common.constant.BoardType;
import com.goodee.beedan.entity.Board;
import com.goodee.beedan.entity.Board_;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.List;

public class BoardSpecs {

    public static Specification<Board> isActive(BoardType type) {
        return (root, query, builder) -> builder.and(
                builder.equal(root.get(Board_.brdTy), type),
                builder.equal(root.get(Board_.brdDelYn), false)
        );
    }

    /**
     * [2] 상태 필터: 접수, 답변완료 등 특정 상태값 검색
     * 값이 있을 때만 쿼리에 참여하며, 없으면 전체 조회로 유기적 동작합니다.
     */
    public static Specification<Board> withStatus(String status) {
        return (root, query, builder) ->
                !StringUtils.hasText(status) ? null : builder.equal(root.get(Board_.brdInqStt), status);
    }

    /**
     * [3] 동적 키워드 검색: 제목, 내용, 작성자 등
     * switch 문을 통해 검색 기준(searchType) 변경에 유연하게 대응합니다.
     */
    public static Specification<Board> withKeyword(String keyword, String searchType) {
        return (root, query, builder) -> {
            // 검색어가 없으면 조건을 생성하지 않음 (null 반환 시 JPA가 무시)
            if (!StringUtils.hasText(keyword)) return null;

            String pattern = "%" + keyword + "%";

            // searchType에 따른 안전한 화이트리스트 분기 로직
            return switch (searchType != null ? searchType : "all") {
                case "title" -> builder.like(root.get(Board_.brdTtl), pattern);
                case "content" -> builder.like(root.get(Board_.brdCon), pattern);
                case "writer" -> builder.like(root.get(Board_.member).get("memNm"), pattern);

                // 검색 타입이 지정되지 않았거나 '전체'일 경우: 제목 OR 내용 통합 검색
                default -> builder.or(
                        builder.like(root.get(Board_.brdTtl), pattern),
                        builder.like(root.get(Board_.brdCon), pattern)
                );
            };
        };
    }

    public static Specification<Board> fetchMember() {
        return (root, query, builder) -> {
            // [중요] count 쿼리일 때는 fetch를 수행하지 않도록 방어 로직 추가
            // count 쿼리의 반환 타입은 보통 Long이므로 이를 체크합니다.
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch(Board_.MEMBER, JoinType.LEFT);
            }
            return null; // 조건(Predicate)은 추가하지 않고 fetch만 수행
        };
    }

    public static Specification<Board> notInIds(List<Long> excludedIds) {
        return (root, query, builder) -> {
            if (excludedIds == null || excludedIds.isEmpty()) return null;
            // id NOT IN (...) 조건 생성
            return builder.not(root.get(Board_.brdId).in(excludedIds));
        };
    }
}