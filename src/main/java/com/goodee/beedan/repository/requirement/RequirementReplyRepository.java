package com.goodee.beedan.repository.requirement;

import com.goodee.beedan.entity.RequirementReply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RequirementReplyRepository extends JpaRepository<RequirementReply, Long> {
    Optional<RequirementReply> findByReqId(Long reqId);
}
