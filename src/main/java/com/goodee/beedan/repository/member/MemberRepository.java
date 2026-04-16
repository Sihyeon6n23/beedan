package com.goodee.beedan.repository.member;

import com.goodee.beedan.common.constant.MemberAuthority;
import com.goodee.beedan.common.constant.MemberBizStatus;
import com.goodee.beedan.common.constant.MemberStatus;
import com.goodee.beedan.config.exception.EntityNotFoundException;
import com.goodee.beedan.config.exception.MemberNotFoundException;
import com.goodee.beedan.dto.member.MemberApproveDto;
import com.goodee.beedan.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query("SELECT m FROM Member m WHERE m.memAut = 'USER' AND m.memStt != 'WITHDRAWN'")
    Page<Member> findAllUsers(Pageable pageable);

    @Query("SELECT m FROM Member m WHERE m.memAut = 'USER' AND m.memStt = :status")
    Page<Member> findUsersByStatus(String status, Pageable pageable);

    @Query("SELECT m FROM Member m " + "WHERE m.memAut = 'USER' " + "AND m.memStt != 'WITHDRAWN' AND " +
            "(LOWER(m.memNm) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(m.memBizTtl) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(m.memCeoNm) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Member> findUsersByKeyword(String keyword, Pageable pageable);

    @Query("SELECT m FROM Member m WHERE m.memAut = 'USER' " + "AND m.memStt = :status AND " +
            "m.memStt != 'WITHDRAWN' AND " +
            "(LOWER(m.memNm) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(m.memBizTtl) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(m.memCeoNm) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Member> findUsersByStatusAndKeyword(String status, String keyword, Pageable pageable);

    List<Member> findByMemIdIn(Collection<Long> memIds);

    @Query("SELECT new com.goodee.beedan.dto.member.MemberApproveDto(" +
            "m.memId, m.memNm, m.memBizNo, m.memCreDt, f.filePat, f.fileUuid) " +
            "FROM Member m " +
            "LEFT JOIN FileUpload f ON m.memId = f.brdRefNo AND f.brdRefTy = 'SIGNUP' AND f.fileDelYn = false " +
            "WHERE m.memBizStt = :bizStt " +
            "AND m.memStt != :memStt " +
            "ORDER BY m.memCreDt DESC")
    List<MemberApproveDto> findBizPendingMembersWithFiles(
            @Param("bizStt") String bizStt,
            @Param("memStt") String memStt
    );
  
    long countByMemCreDtBetween(java.time.LocalDateTime from, java.time.LocalDateTime to);

    // 월별 가입자 수 (GROUP BY)
    @Query("SELECT MONTH(m.memCreDt), COUNT(m) FROM Member m WHERE m.memCreDt BETWEEN :from AND :to GROUP BY MONTH(m.memCreDt)")
    List<Object[]> countGroupByMonth(@org.springframework.data.repository.query.Param("from") java.time.LocalDateTime from,
                                     @org.springframework.data.repository.query.Param("to") java.time.LocalDateTime to);

    boolean existsByMemEml(String memEml);

    default Member getByIdOrThrow(Long memId) {
        return findById(memId).orElseThrow(() -> new MemberNotFoundException("해당 사용자를 찾을 수 없습니다. ID: " + memId));
    }
    boolean existsByMemMbPhn(String phoneNumber);

    boolean existsByMemBizNoAndMemBizStt(String bizNo, String bizStatus);

}
