package com.goodee.beedan.repository.chatbot;

import com.goodee.beedan.entity.ChatbotResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatbotResponseRepository extends JpaRepository<ChatbotResponse, Long> {
    // ===== 조회용 메서드 =====

    // 특정 질의에 연결된 활성 최종 응답 1건 조회
    // - cbTpId = 질의 id, cbResUseYn = true
    Optional<ChatbotResponse> findByCbTpIdAndCbResUseYn(Long cbTpId, boolean cbResUseYn);

    // 특정 질의에 활성 응답이 존재하는지 확인
    // - 하위 질의가 없을 때 최종 응답으로 바로 갈 수 있는지 판단
    boolean existsByCbTpIdAndCbResUseYn(Long cbTpId, boolean cbResUseYn);


    // ===== 등록/수정 확장용 메서드 =====

    // 질의 1개당 응답 1개 규칙 검증
    // - 등록 시 같은 질의 id 를 가진 응답이 이미 있는지 확인
    boolean existsByCbTpId(Long cbTpId);

    // 수정 시 자기 자신을 제외하고 같은 질의 id 를 가진 응답이 있는지 확인
    boolean existsByCbTpIdAndCbResIdNot(Long cbTpId, Long cbResId);

    // 특정 응답이 활성 상태인지 포함해서 단건 조회
    // - 수정/비활성화 전에 현재 데이터 확인
    Optional<ChatbotResponse> findByCbResIdAndCbResUseYn(Long cbResId, boolean cbResUseYn);
}
