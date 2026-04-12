package com.goodee.beedan.repository.member;

import com.goodee.beedan.dto.member.MemberApproveDto;
import com.goodee.beedan.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByMemLgnId(String memLgnId);
    Boolean existsByMemLgnId(String memLgnId);
    Optional<Member> findByMemNmAndMemEml(String MemNm, String MemEml);
    Optional<Member> findByMemLgnIdAndMemEml(String MemLgnId, String MemEml);

    @Query("SELECT m FROM Member m WHERE m.memAut = 'USER'")
    Page<Member> findAllUsers(Pageable pageable);

    @Query("SELECT m FROM Member m WHERE m.memAut = 'USER' AND m.memStt = :status")
    Page<Member> findUsersByStatus(String status, Pageable pageable);

    List<Member> findByMemIdIn(Collection<Long> memIds);

    @Query("SELECT new com.goodee.beedan.dto.member.MemberApproveDto(" +
            "m.memId, m.memNm, m.memBizNo, m.memCreDt, f.filePat, f.fileUuid) " +
            "FROM Member m " +
            "LEFT JOIN FileUpload f ON m.memId = f.brdRefNo AND f.brdRefTy = 'SIGNUP' AND f.fileDelYn = false " +
            "WHERE m.memStt = 'PENDING' " +
            "ORDER BY m.memCreDt DESC")
    List<MemberApproveDto> findPendingMembersWithFiles();

    boolean existsByMemEml(String memEml);
}
