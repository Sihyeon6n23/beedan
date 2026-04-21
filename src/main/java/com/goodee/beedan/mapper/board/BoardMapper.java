package com.goodee.beedan.mapper.board;

import com.goodee.beedan.dto.board.notice.CommonBoardDetailDto;
import com.goodee.beedan.entity.Board;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BoardMapper {

    @Mapping(source = "brdId", target = "brdId")
    @Mapping(source = "brdTtl", target = "brdTtl") // 제목 누락 방지
    @Mapping(source = "brdCon", target = "brdCon")
    @Mapping(source = "brdCreDt", target = "brdCreDt") // 작성일 누락 방지
    @Mapping(source = "brdVstCnt", target = "brdHit")

    // 회원 및 상호명 정보
    @Mapping(source = "member.memId", target = "memId")
    @Mapping(source = "member.memNm", target = "memNm")
    @Mapping(source = "member.memBizTtl", target = "memBizTtl")

    // --- [추가 핵심 필드] ---
    @Mapping(source = "brdInqStt", target = "brdInqStt") // 문의 상태 (Enum)
    @Mapping(source = "brdCanRe", target = "brdCanRe")   // 취소/반려 사유
    @Mapping(source = "brdFixYn", target = "brdFixYn")   // 공지사항 고정 여부

    // --- [목록용 유틸리티 필드] ---
    // 24시간 이내 글인지 여부 계산 (MapStruct expression 활용)
    @Mapping(target = "isNew", expression = "java(board.getBrdCreDt().isAfter(java.time.LocalDateTime.now().minusDays(1)))")
    @Mapping(target = "fileYn", ignore = true) // 서비스에서 첨부파일 존재 여부 배치 조회 후 세팅

    // --- [서비스 세팅 필드 (Ignore)] ---
    @Mapping(target = "fileList", ignore = true)
    @Mapping(target = "prevBoard", ignore = true)
    @Mapping(target = "nextBoard", ignore = true)
    @Mapping(target = "reply", ignore = true)
    @Mapping(target = "canEdit", ignore = true)
    @Mapping(target = "canCancel", ignore = true)
    @Mapping(target = "canAnswer", ignore = true)
    @Mapping(target = "canUpdateStatus", ignore = true) // 지난 대화에서 추가하기로 한 필드

    @Mapping(target = "replyEdited", ignore = true)
    @Mapping(target = "commentCount", ignore = true)
    CommonBoardDetailDto toDetailDto(Board board);
}