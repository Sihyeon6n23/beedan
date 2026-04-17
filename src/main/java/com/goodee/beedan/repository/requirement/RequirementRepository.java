package com.goodee.beedan.repository.requirement;


import com.goodee.beedan.dto.requirement.RequirementListDto;
import com.goodee.beedan.entity.Requirement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface RequirementRepository extends JpaRepository<Requirement, Long>,
        JpaSpecificationExecutor<Requirement> {
    Page<Requirement> findByReqDelYnFalseAndReqStt(String reqStt, Pageable pageable);

    Page<Requirement> findByReqDelYnFalseAndMemId(Long memId, Pageable pageable);

    Page<Requirement> findByReqDelYnFalseAndMemIdAndReqStt(Long memId, String reqStt, Pageable pageable);


    Page<Requirement> findByReqDelYnFalseAndReqSttAndReqRepYn(String submitted, boolean b, Pageable pageable);

    Page<Requirement> findByReqDelYnFalseAndMemIdAndReqSttAndReqRepYn(Long memId, String submitted, boolean b, Pageable pageable);

    // keyword 검색용
    Page<Requirement> findByReqDelYnFalseAndReqSttAndReqTtlContaining(String reqStt, String keyword, Pageable pageable);
    Page<Requirement> findByReqDelYnFalseAndReqSttAndReqRepYnAndReqTtlContaining(String reqStt, boolean repYn, String keyword, Pageable pageable);
    Page<Requirement> findByReqDelYnFalseAndMemIdAndReqTtlContaining(Long memId, String keyword, Pageable pageable);
    Page<Requirement> findByReqDelYnFalseAndMemIdAndReqSttAndReqTtlContaining(Long memId, String reqStt, String keyword, Pageable pageable);
    Page<Requirement> findByReqDelYnFalseAndMemIdAndReqSttAndReqRepYnAndReqTtlContaining(Long memId, String reqStt, boolean repYn, String keyword, Pageable pageable);

    Requirement findByReqId(Long reqId);
}
