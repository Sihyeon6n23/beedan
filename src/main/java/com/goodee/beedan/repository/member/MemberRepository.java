package com.goodee.beedan.repository.member;

import com.goodee.beedan.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
}
