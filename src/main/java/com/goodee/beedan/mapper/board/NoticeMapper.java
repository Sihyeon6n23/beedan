package com.goodee.beedan.mapper.board;

import com.goodee.beedan.dto.board.notice.NoticeDetailDto;
import com.goodee.beedan.dto.board.notice.NoticeListDto;
import com.goodee.beedan.entity.Board;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface NoticeMapper {

    /**
     * 엔티티 -> 목록용 DTO 변환
     */
    // NoticeListDto의 조회수 필드명이 brdHit라면 그대로 유지, 아니라면 수정 필요
    @Mapping(target = "fileYn", ignore = true)
    @Mapping(source = "brdVstCnt", target = "brdHit")
    @Mapping(source = "member.memNm", target = "memNm")
    NoticeListDto toListDto(Board notice);

    /**
     * 엔티티 -> 상세조회용 DTO 변환
     */
    // NoticeDetailDto에는 brdVstCnt 필드가 있으므로 target을 brdVstCnt로 수정
    @Mapping(source = "member.memId", target = "memId")
    @Mapping(source = "member.memNm", target = "memNm")
    @Mapping(target = "prevNotice", ignore = true)
    @Mapping(target = "nextNotice", ignore = true)
    @Mapping(target = "fileList", ignore = true)
    @Mapping(target = "canModify", ignore = true)
    NoticeDetailDto toDetailDto(Board notice);
}
