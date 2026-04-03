package com.goodee.beedan.service.requirement;

import com.goodee.beedan.dto.requirement.RequirementListDto;
import com.goodee.beedan.entity.Requirement;
import com.goodee.beedan.repository.requirement.RequirementRepository;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class RequirementService {

    private final RequirementRepository requirementRepository;

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
                pages = requirementRepository.findByReqDelYnFalseAndMemberMemIdAndReqStt(memId, "DRAFT", pageable);
                break;
            case "SUBMITTED":
                pages = requirementRepository.findByReqDelYnFalseAndMemberMemIdAndReqSttAndReqRepYn(memId, "SUBMITTED", false, pageable);
                break;
            case "ANSWERED":
                pages = requirementRepository.findByReqDelYnFalseAndMemberMemIdAndReqSttAndReqRepYn(memId, "SUBMITTED", true, pageable);
                break;
            default:
                pages = requirementRepository.findByReqDelYnFalseAndMemberMemId(memId, pageable);
                break;
        }

        return pages.map(this::mapToListDto);
    }

    // requirement -> dto 로 매핑
    private RequirementListDto mapToListDto(Requirement requirement) {
        return RequirementListDto.builder()
                .reqId(requirement.getReqId())
                .memId(requirement.getMember().getMemId())
                .reqTtl(requirement.getReqTtl())
                .reqStt(requirement.getReqStt())
                .reqPerYn(requirement.getReqPerYn())
                .reqRepYn(requirement.getReqRepYn())
                .reqCreDt(requirement.getReqCreDt())
                .memStt(requirement.getMember().getMemStt())
                .memNm(requirement.getMember().getMemNm())
                .memBizTtl(requirement.getMember().getMemBizTtl())
                .build();
    }


}
