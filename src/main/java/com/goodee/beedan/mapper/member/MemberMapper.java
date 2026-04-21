package com.goodee.beedan.mapper.member;

import com.goodee.beedan.dto.member.mypage.UpdateMemberRequest;
import com.goodee.beedan.entity.Member;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MemberMapper {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(source = "name", target = "memNm")
    @Mapping(source = "memEml", target = "memEml")
    @Mapping(source = "memMbPhn", target = "memMbPhn")
    @Mapping(source = "memCi", target = "memCi")
    // 주소 필드 매핑 추가
    @Mapping(source = "postCode", target = "memPosCd")
    @Mapping(source = "address", target = "memBizAdr")
    @Mapping(source = "addressDetail", target = "memBizDtAdr")
    void updateEntityFromDto(UpdateMemberRequest dto, @MappingTarget Member entity);

}