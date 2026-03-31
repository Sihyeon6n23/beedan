package com.goodee.beedan.repository.receiver;

import com.goodee.beedan.entity.Receiver;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReceiverRepository extends JpaRepository<Receiver, Long> {
    public List<Receiver> findByMember_memIdOrderByRcIdAsc(Long memId);
    public Receiver findFirstByMember_memIdOrderByRcIdAsc(Long memId);
    public Boolean existsByMember_memId(Long memID);
    // 기본 배송지 조회 (rcAdrDfYn = true, 삭제되지 않은 것)
    Receiver findFirstByMember_memIdAndRcAdrDfYnTrueAndRcDelYnFalse(Long memId);
    // 회원의 활성 배송지 목록
    List<Receiver> findByMember_memIdAndRcDelYnFalseOrderByRcAdrDfYnDescRcIdAsc(Long memId);
}
