package com.goodee.beedan.repository.member;

import com.goodee.beedan.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByMemLgnId(String memLgnId);
    Boolean existsByMemLgnId(String memLgnId);
    Optional<Member> findByMemNmAndMemEml(String MemNm, String MemEml);
    Optional<Member> findByMemLgnIdAndMemEml(String MemLgnId, String MemEml);
}
