package com.goodee.beedan.repository.receiver;

import com.goodee.beedan.entity.Receiver;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ReceiverRepository extends JpaRepository<Receiver, Long> {
    boolean existsByMember_memId(Long memId);

    List<Receiver> findByMember_memIdAndRcDelYnFalseOrderByRcAdrDfYnDescRcIdAsc(Long memId);

    Optional<Receiver> findByRcIdAndMember_memIdAndRcDelYnFalse(Long rcId, Long memId); // (권한 확인 + 삭제된 것 제외)

    Receiver findFirstByMember_memIdAndRcAdrDfYnTrueAndRcDelYnFalse(Long memId);

}
