package com.goodee.beedan.mapper.member;

import com.goodee.beedan.dto.admin.MemberEditRequest;
import com.goodee.beedan.entity.Member;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MemberEditRequestToMemberMapper {

    /**
     * DTO의 정보를 기존 Member 엔티티 객체에 덮어씌웁니다 (Update)
     * @Mapping target은 Member 엔티티의 필드명, source는 DTO의 필드명입니다.
     */
    @Mapping(target = "memCeoPhn", source = "phone")
    @Mapping(target = "memNm", source = "managerName")
    @Mapping(target = "memMbPhn", source = "managerPhone")
    @Mapping(target = "memEml", source = "email")
    @Mapping(target = "memPosCd", source = "postCode")
    @Mapping(target = "memBizAdr", source = "address")
    @Mapping(target = "memBizDtAdr", source = "addressDetail")
    @Mapping(target = "memBizStt", source = "bizStatus")
    @Mapping(target = "memStt", source = "status")
    void updateMemberFromDto(MemberEditRequest dto, @MappingTarget Member member);
}