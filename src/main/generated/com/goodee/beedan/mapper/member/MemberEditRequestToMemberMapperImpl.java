package com.goodee.beedan.mapper.member;

import com.goodee.beedan.dto.admin.MemberEditRequest;
import com.goodee.beedan.entity.Member;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-12T15:42:16+0900",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.8 (Oracle Corporation)"
)
@Component
public class MemberEditRequestToMemberMapperImpl implements MemberEditRequestToMemberMapper {

    @Override
    public void updateMemberFromDto(MemberEditRequest dto, Member member) {
        if ( dto == null ) {
            return;
        }

        member.setMemCeoPhn( dto.getPhone() );
        member.setMemNm( dto.getManagerName() );
        member.setMemMbPhn( dto.getManagerPhone() );
        member.setMemEml( dto.getEmail() );
        member.setMemPosCd( dto.getPostCode() );
        member.setMemBizAdr( dto.getAddress() );
        member.setMemBizDtAdr( dto.getAddressDetail() );
        member.setMemStt( dto.getStatus() );
    }
}
