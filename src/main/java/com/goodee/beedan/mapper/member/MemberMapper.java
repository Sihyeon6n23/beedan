package com.goodee.beedan.mapper.member;

import com.goodee.beedan.dto.member.EditMemberDto;
import com.goodee.beedan.entity.Member;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface MemberMapper {

    void updateEntityFromDto(EditMemberDto dto, @MappingTarget Member entity);
}
