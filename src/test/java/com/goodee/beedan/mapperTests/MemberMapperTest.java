package com.goodee.beedan.mapperTests;

import com.goodee.beedan.dto.member.EditMemberDto;
import com.goodee.beedan.entity.Member;
import com.goodee.beedan.mapper.member.MemberMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class MemberMapperTest {

    @Autowired
    private MemberMapper memberMapper;

    private Member entity;

    @BeforeEach
    void setUp() {
        // 매 테스트가 실행되기 전, DB에서 조회해 온 '기존 회원 정보'라고 가정하고 세팅합니다.
        entity = Member.builder()
                .memEml("old@test.com")
                .memCeoPhn("010-0000-0000")
                .memCmpTel("02-000-0000")
                .memPosCd("12345")
                .memBizAdr("기존 주소")
                .memBizDtAdr("기존 상세주소")
                .memNm("기존이름") // DTO에 없는 필드 (절대 변경되면 안 됨)
                .build();
    }

    @Test
    @DisplayName("1. DTO의 모든 값이 null일 때")
    void updateWithAllNullValues() {
        // given: 모든 필드가 null인 깡통 DTO
        EditMemberDto dto = new EditMemberDto();

        // when: 맵핑 실행
        memberMapper.updateEntityFromDto(dto, entity);

        // then: DTO에 있는 필드들은 모두 null로 덮어씌워짐 (현재 매퍼 설정 기준)
        assertEquals("old@test.com", entity.getMemEml());
        assertEquals("010-0000-0000", entity.getMemCeoPhn());
        assertEquals("02-000-0000", entity.getMemCmpTel());
        assertEquals("12345", entity.getMemPosCd());
        assertEquals("기존 주소", entity.getMemBizAdr());
        assertEquals("기존 상세주소", entity.getMemBizDtAdr());
        assertEquals("기존이름", entity.getMemNm());
    }

    @Test
    @DisplayName("2. DTO에 모든 값이 있을 때")
    void updateWithAllValues() {
        // given: 모든 필드에 새로운 값이 꽉 찬 DTO
        EditMemberDto dto = EditMemberDto.builder()
                .memEml("new@test.com")
                .memCeoPhn("010-9999-9999")
                .memCmpTel("02-999-9999")
                .memPosCd("54321")
                .memBizAdr("새로운 주소")
                .memBizDtAdr("새로운 상세주소")
                .build();

        // when: 맵핑 실행
        memberMapper.updateEntityFromDto(dto, entity);

        // then: 모든 필드가 DTO의 새로운 값으로 정상 업데이트 됨
        assertEquals("new@test.com", entity.getMemEml());
        assertEquals("010-9999-9999", entity.getMemCeoPhn());
        assertEquals("02-999-9999", entity.getMemCmpTel());
        assertEquals("54321", entity.getMemPosCd());
        assertEquals("새로운 주소", entity.getMemBizAdr());
        assertEquals("새로운 상세주소", entity.getMemBizDtAdr());
    }

    @Test
    @DisplayName("3. DTO의 위에 3개 필드만 값이 있을 때 (나머지는 null)")
    void updateWithTopThreeValues() {
        // given: 위 3개 필드만 세팅하고, 나머지는 null인 DTO
        EditMemberDto dto = EditMemberDto.builder()
                .memEml("top3@test.com")
                .memCeoPhn("010-3333-3333")
                .memCmpTel("02-333-3333")
                // posCd, bizAdr, bizDtAdr 은 세팅하지 않음 (null 상태)
                .build();

        // when: 맵핑 실행
        memberMapper.updateEntityFromDto(dto, entity);

        // then: 위 3개는 업데이트 됨
        assertEquals("top3@test.com", entity.getMemEml());
        assertEquals("010-3333-3333", entity.getMemCeoPhn());
        assertEquals("02-333-3333", entity.getMemCmpTel());
    }
}
