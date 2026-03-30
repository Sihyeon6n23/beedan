package com.goodee.beedan.repository.receiver;

import com.goodee.beedan.entity.Receiver;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReceiverRepository extends JpaRepository<Receiver, Long> {
    public List<Receiver> findByMember_memIdOrderByRcIdAsc(Long memId);
    public Boolean existsByMember_memId(Long memID);
}
