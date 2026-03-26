package com.goodee.beedan.repository.chatbot;

import com.goodee.beedan.entity.ChatbotTopic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatbotTopicRepository extends JpaRepository<ChatbotTopic, Long> {
    // ===== 조회용 메서드 =====

    // 1차 질의 버튼 목록 조회
    // cbTpLvl = 1, cbTpUseYn = true, cbTpOrd 오름차순 정렬
    List<ChatbotTopic> findByCbTpLvlAndCbTpUseYnOrderByCbTpOrdAsc(Integer cbTpLvl, boolean cbTpUseYn);

    // 특정 1차 질의의 2차 질의 버튼 목록 조회
    // - cbTpPrnId = 부모 1차 질의 id, cbTpLvl = 2, cbTpUseYn = true, cbTpOrd 오름차순 정렬
    List<ChatbotTopic> findByCbTpPrnIdAndCbTpLvlAndCbTpUseYnOrderByCbTpOrdAsc(Long cbTpPrnId, Integer cbTpLvl, boolean cbTpUseYn);

    // 특정 질의 단건 조회
    // - 사용자가 클릭한 질의가 실제 존재하는지 확인
    // - 비활성 질의는 제외하려면 useYn 조건도 함께 둔다
    Optional<ChatbotTopic> findByCbTpIdAndCbTpUseYn(Long cbTpId, boolean cbTpUseYn);

    // 특정 부모 아래에 활성 하위 질의가 있는지 확인
    // - 2차 버튼을 보여줄지 아니면 최종 응답으로 바로 갈지 판단
    boolean existsByCbTpPrnIdAndCbTpLvlAndCbTpUseYn(Long cbTpPrnId, Integer cbTpLvl, boolean cbTpUseYn);


    // ===== 등록/수정 확장용 메서드 =====

    // 1차 질의 순서 중복 검사
    // - 1차 질의는 cbTpPrnId 가 NULL 이라 DB UNIQUE 만으로는 순서 중복 방지가 불완전할 수 있음
    // - 저장/수정 전에 서비스에서 한 번 더 확인하는 용도
    boolean existsByCbTpLvlAndCbTpOrd(Integer cbTpLvl, Integer cbTpOrd);

    // 2차 질의 순서 중복 검사
    boolean existsByCbTpPrnIdAndCbTpLvlAndCbTpOrd(Long cbTpPrnId, Integer cbTpLvl, Integer cbTpOrd);

    // 특정 부모 아래 하위 질의 개수 확인
    // - 관리자 기능 확장 시 부모 비활성화/삭제 전 점검
    long countByCbTpPrnId(Long cbTpPrnId);
}
