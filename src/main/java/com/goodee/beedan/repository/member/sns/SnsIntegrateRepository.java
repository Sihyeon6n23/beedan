package com.goodee.beedan.repository.member.sns;

import com.goodee.beedan.entity.Member;
import com.goodee.beedan.entity.SnsIntegrate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SnsIntegrateRepository extends JpaRepository<SnsIntegrate, Long> {
    Optional<SnsIntegrate> findBySnsCanYnFalseAndMember(Member member);
    Optional<SnsIntegrate> findBySnsCanYnFalseAndSnsSeNo(String SnsSeNo);
    Boolean existsBySnsCanYnFalseAndMember(Member member);
}

