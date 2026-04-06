package com.goodee.beedan.service.requirement;

import com.goodee.beedan.dto.requirement.RequireForm;
import com.goodee.beedan.dto.requirement.RequirementListDto;
import com.goodee.beedan.entity.Requirement;
import com.goodee.beedan.repository.member.MemberRepository;
import com.goodee.beedan.repository.requirement.RequirementRepository;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class RequirementService {

    private final RequirementRepository requirementRepository;
    private final MemberRepository memberRepository;

    // 제출 완료된, 삭제되지 않은 요청서 전부 조회 (관리자용)
    public Page<RequirementListDto> findAllForAdmin(String status, Pageable pageable) {
        Page<Requirement> pages;

        switch (status) {
            case "SUBMITTED":
                pages = requirementRepository.findByReqDelYnFalseAndReqSttAndReqRepYn("SUBMITTED", false, pageable);
                break;
            case "ANSWERED":
                pages = requirementRepository.findByReqDelYnFalseAndReqSttAndReqRepYn("SUBMITTED", true, pageable);
                break;
            default:
                pages = requirementRepository.findByReqDelYnFalseAndReqStt("SUBMITTED", pageable);
                break;
        }

        return pages.map(this::mapToListDto);
    }

    // 사용자 전용 요청 목록 조회
    public Page<RequirementListDto> findAllForUser(Long memId, String status, Pageable pageable) {
        Page<Requirement> pages;

        switch (status) {
            case "DRAFT":
                pages = requirementRepository.findByReqDelYnFalseAndMemIdAndReqStt(memId, "DRAFT", pageable);
                break;

            case "SUBMITTED":
                pages = requirementRepository.findByReqDelYnFalseAndMemIdAndReqSttAndReqRepYn(memId, "SUBMITTED", false, pageable);
                break;
            case "ANSWERED":
                pages = requirementRepository.findByReqDelYnFalseAndMemIdAndReqSttAndReqRepYn(memId, "SUBMITTED", true, pageable);
                break;
            default:
                pages = requirementRepository.findByReqDelYnFalseAndMemId(memId, pageable);
                break;
        }

        return pages.map(this::mapToListDto);
    }

    // 사용자: new requriement 작성

    public void submitRequirement(Long memberId, RequireForm requireForm) {
        String memNm = memberRepository.findById(memberId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다.")).getMemNm();
        String memBizTtl = memberRepository.findById(memberId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다.")).getMemBizTtl();

        Requirement requirement = Requirement.builder()
                .memId(memberId)
                .memNm(memNm)
                .memBizTtl(memBizTtl)
                .reqTtl(requireForm.getReqTtl())
                .reqCon(requireForm.getReqCnt())
                .reqRef(requireForm.getReqRef())
                .reqPr(requireForm.getReqPr())
                .reqCur(requireForm.getReqCur())
                .reqStt("SUBMITTED")
                .reqRepYn(false)
                .reqCreDt(LocalDateTime.now())
                .reqDelYn(false)
                .build();

        requirementRepository.save(requirement);
    }



    public Long draftRequirement(Long memberId, RequireForm requireForm) {
        String memNm = memberRepository.findById(memberId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다.")).getMemNm();
        String memBizTtl = memberRepository.findById(memberId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다.")).getMemBizTtl();

        Requirement requirement = Requirement.builder()
                .memId(memberId)
                .memNm(memNm)
                .memBizTtl(memBizTtl)
                .reqTtl(requireForm.getReqTtl())
                .reqCon(requireForm.getReqCnt())
                .reqRef(requireForm.getReqRef())
                .reqPr(requireForm.getReqPr())
                .reqCur(requireForm.getReqCur())
                .reqStt("DRAFT")
                .reqRepYn(false)
                .reqCreDt(LocalDateTime.now())
                .reqDelYn(false)
                .build();

        return requirementRepository.save(requirement).getReqId();
    }

    // 상세 조회
    public RequireForm getRequireForm(Long reqId) {
        Requirement r = requirementRepository.findById(reqId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 요청입니다."));
        RequireForm form = new RequireForm();
        form.setReqId(r.getReqId());
        form.setMemId(r.getMemId());
        form.setMemNm(r.getMemNm());
        form.setMemBizTtl(r.getMemBizTtl());
        form.setReqTtl(r.getReqTtl());
        form.setReqCnt(r.getReqCon());
        form.setReqRef(r.getReqRef());
        form.setReqPr(r.getReqPr());
        form.setReqCur(r.getReqCur());
        form.setReqStt(r.getReqStt());
        form.setReqPerYn(r.getReqPerYn());
        form.setReqRepYn(r.getReqRepYn());
        return form;
    }

    // 삭제 (논리 삭제)
    public void deleteRequirement(Long reqId) {
        Requirement r = requirementRepository.findById(reqId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 요청입니다."));
        r.setReqDelYn(true);
        requirementRepository.save(r);
    }

    // DRAFT → SUBMITTED 제출
    public void submitDraft(Long reqId) {
        Requirement r = requirementRepository.findById(reqId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 요청입니다."));
        r.setReqStt("SUBMITTED");
        r.setReqUpdDt(LocalDateTime.now());
        requirementRepository.save(r);
    }

    // SUBMITTED → 등록 취소 (삭제 처리)
    public void cancelRequirement(Long reqId) {
        Requirement r = requirementRepository.findById(reqId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 요청입니다."));
        r.setReqDelYn(true);
        requirementRepository.save(r);
    }

    // requirement -> dto 로 매핑
    private RequirementListDto mapToListDto(Requirement requirement) {
        String memStt = memberRepository.findById(requirement.getMemId()).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다.")).getMemStt();
        return RequirementListDto.builder()
                .reqId(requirement.getReqId())
                .memId(requirement.getMemId())
                .reqTtl(requirement.getReqTtl())
                .reqStt(requirement.getReqStt())
                .reqPerYn(requirement.getReqPerYn())
                .reqRepYn(requirement.getReqRepYn())
                .reqCreDt(requirement.getReqCreDt())
                .memStt(memStt)
                .memNm(requirement.getMemNm())
                .memBizTtl(requirement.getMemBizTtl())
                .build();
    }
}
