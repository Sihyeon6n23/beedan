package com.goodee.beedan.repository.payment;

import com.goodee.beedan.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByQuId(Long quId);

    Optional<Payment> findByPyPgNm(String pyPgNm);

    List<Payment> findAllByMemIdOrderByPyPdAtDesc(Long memId);

    // 결제 + 견적코드 + 협상명 조인 조회 (N+1 제거)
    @org.springframework.data.jpa.repository.Query(
            "SELECT p, q.quCd, n.ngNm FROM Payment p " +
            "JOIN QuoteBase q ON p.quId = q.quId " +
            "JOIN Negotiation n ON q.ngId = n.ngId " +
            "WHERE p.memId = :memId ORDER BY p.pyPdAt DESC")
    java.util.List<Object[]> findAllWithQuoteInfoByMemId(@org.springframework.data.repository.query.Param("memId") Long memId);
}
