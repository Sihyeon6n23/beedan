package com.goodee.beedan.mapper.member;

import com.goodee.beedan.dto.member.mypage.UpdateMemberRequest;
import com.goodee.beedan.entity.Member;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-16T13:20:46+0900",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.9 (Oracle Corporation)"
)
@Component
public class MemberMapperImpl implements MemberMapper {

    @Override
    public void updateEntityFromDto(UpdateMemberRequest dto, Member entity) {
        if ( dto == null ) {
            return;
        }

        if ( dto.getName() != null ) {
            entity.setMemNm( dto.getName() );
        }
        if ( dto.getEmail() != null ) {
            entity.setMemEml( dto.getEmail() );
        }
        if ( dto.getPhone() != null ) {
            entity.setMemMbPhn( dto.getPhone() );
        }
        if ( dto.getCi() != null ) {
            entity.setMemCi( dto.getCi() );
        }
        if ( dto.getPostCode() != null ) {
            entity.setMemPosCd( dto.getPostCode() );
        }
        if ( dto.getAddress() != null ) {
            entity.setMemBizAdr( dto.getAddress() );
        }
        if ( dto.getAddressDetail() != null ) {
            entity.setMemBizDtAdr( dto.getAddressDetail() );
        }
    }
}
