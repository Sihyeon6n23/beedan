package com.goodee.beedan.repository.payment;

import com.goodee.beedan.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByQuId(Long quId);

    Optional<Payment> findByPyPgNm(String pyPgNm);
}
