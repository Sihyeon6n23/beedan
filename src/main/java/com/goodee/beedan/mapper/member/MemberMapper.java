package com.goodee.beedan.mapper.member;

import com.goodee.beedan.dto.member.mypage.UpdateMemberRequest;
import com.goodee.beedan.entity.Member;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface MemberMapper {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(source = "name", target = "memNm")    // DTO의 email -> Entity의 memEml
    @Mapping(source = "email", target = "memEml")    // DTO의 email -> Entity의 memEml
    @Mapping(source = "phone", target = "memMbPhn")  // DTO의 phone -> Entity의 memMbPhn
    @Mapping(source = "ci", target = "memCi")        // DTO의 ci -> Entity의 memCi
    void updateEntityFromDto(UpdateMemberRequest dto, @MappingTarget Member entity);

}