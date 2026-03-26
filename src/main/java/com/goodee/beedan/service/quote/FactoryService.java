package com.goodee.beedan.service.quote;

import com.goodee.beedan.dto.quote.FactoryRequest;
import com.goodee.beedan.entity.Factory;
import com.goodee.beedan.repository.quote.FactoryRepository;
import groovy.util.logging.Slf4j;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@lombok.extern.slf4j.Slf4j
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FactoryService {

    private final FactoryRepository factoryRepository;

    /**
     * 공장 등록
     */
    @Transactional
    public Factory create(FactoryRequest request) {
        Factory factory = factoryRepository.save(
                Factory.builder()
                        .brId(request.getBrId())
                        .faNm(request.getFaNm())
                        .faAd(request.getFaAd())
                        .faCty(request.getFaCty())
                        .faCCd(request.getFaCCd())
                        .build()
        );
        log.info("공장 등록 완료. ID: {}, 공장명: {}, 국가코드: {}",
                factory.getFaId(), factory.getFaNm(), factory.getFaCCd());
        return factory;
    }

    /**
     * 단건 조회
     */
    public Factory findById(Long faId) {
        return factoryRepository.findById(faId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "공장을 찾을 수 없습니다. id: " + faId));
    }

    /**
     * 활성 공장 전체 조회
     */
    public List<Factory> findAllActive() {
        return factoryRepository.findAllByFaYnTrue();
    }

    /**
     * 국가코드별 활성 공장 조회
     */
    public List<Factory> findAllByCountry(String faCCd) {
        return factoryRepository.findAllByFaCCdAndFaYnTrue(faCCd);
    }

    /**
     * 브랜드별 활성 공장 조회
     */
    public List<Factory> findAllByBrand(Long brId) {
        return factoryRepository.findAllByBrIdAndFaYnTrue(brId);
    }

    /**
     * 공장 정보 수정
     */
    @Transactional
    public Factory update(Long faId, FactoryRequest request) {
        Factory factory = findById(faId);
        factory.update(
                request.getFaNm(),
                request.getFaAd(),
                request.getFaCty(),
                request.getFaCCd()
        );
        log.info("공장 수정 완료. ID: {}, 공장명: {}, 국가코드: {}",
                factory.getFaId(), factory.getFaNm(), factory.getFaCCd());
        return factory;
    }

    /**
     * 공장 비활성화
     */
    @Transactional
    public void deactivate(Long faId) {
        Factory factory = findById(faId);
        factory.deactivate();
        log.info("공장 비활성화 완료. ID: {}, 공장명: {}", factory.getFaId(), factory.getFaNm());
    }
}