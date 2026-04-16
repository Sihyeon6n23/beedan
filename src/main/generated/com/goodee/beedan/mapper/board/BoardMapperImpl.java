package com.goodee.beedan.mapper.board;

import com.goodee.beedan.dto.board.notice.CommonBoardDetailDto;
import com.goodee.beedan.entity.Board;
import com.goodee.beedan.entity.Member;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-16T21:30:07+0900",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.8 (Oracle Corporation)"
)
@Component
public class BoardMapperImpl implements BoardMapper {

    @Override
    public CommonBoardDetailDto toDetailDto(Board board) {
        if ( board == null ) {
            return null;
        }

        CommonBoardDetailDto.CommonBoardDetailDtoBuilder commonBoardDetailDto = CommonBoardDetailDto.builder();

        commonBoardDetailDto.brdId( board.getBrdId() );
        commonBoardDetailDto.brdTtl( board.getBrdTtl() );
        commonBoardDetailDto.brdCon( board.getBrdCon() );
        commonBoardDetailDto.brdCreDt( board.getBrdCreDt() );
        commonBoardDetailDto.brdHit( board.getBrdVstCnt() );
        commonBoardDetailDto.memId( boardMemberMemId( board ) );
        commonBoardDetailDto.memNm( boardMemberMemNm( board ) );
        commonBoardDetailDto.memBizTtl( boardMemberMemBizTtl( board ) );
        commonBoardDetailDto.brdInqStt( board.getBrdInqStt() );
        commonBoardDetailDto.brdCanRe( board.getBrdCanRe() );
        commonBoardDetailDto.brdFixYn( board.getBrdFixYn() );

        commonBoardDetailDto.isNew( board.getBrdCreDt().isAfter(java.time.LocalDateTime.now().minusDays(1)) );

        return commonBoardDetailDto.build();
    }

    private Long boardMemberMemId(Board board) {
        Member member = board.getMember();
        if ( member == null ) {
            return null;
        }
        return member.getMemId();
    }

    private String boardMemberMemNm(Board board) {
        Member member = board.getMember();
        if ( member == null ) {
            return null;
        }
        return member.getMemNm();
    }

    private String boardMemberMemBizTtl(Board board) {
        Member member = board.getMember();
        if ( member == null ) {
            return null;
        }
        return member.getMemBizTtl();
    }
}
