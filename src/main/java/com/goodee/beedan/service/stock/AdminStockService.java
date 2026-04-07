package com.goodee.beedan.service.stock;

import com.goodee.beedan.dto.stock.AdminStockDto;
import com.goodee.beedan.entity.Stock;
import com.goodee.beedan.repository.stock.StockRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminStockService {

    private final StockRepository stockRepository;

    // 첫 호출 + 검색 조건 적용 조회
    public Page<AdminStockDto> findAdminStocks(String keyword,
                                               LocalDateTime startDate,
                                               LocalDateTime endDate,
                                               Pageable pageable) {
        Specification<Stock> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("stDelYn"))); // 삭제된 상품 제외

            if (keyword != null && !keyword.isBlank()) {
                String pattern = "%" + keyword.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("stNm")), pattern),
                        cb.like(cb.lower(root.get("stBrNm")), pattern),
                        cb.like(cb.lower(root.get("stCd")), pattern)
                ));
            }
            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("stCraDt"), startDate));
            }
            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("stCraDt"), endDate));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return stockRepository.findAll(spec, pageable)
                .map(this::mapToAdminStockDto);  // 엔티티→DTO 변환 메서드 필요
    }

    // 노출 여부 수정 by 토글 버튼
    public boolean toggleExpYn(Long stId) {
        Stock stock = stockRepository.findById(stId).orElseThrow(() -> new NoSuchElementException("존재하지 않는 상품입니다."));
        stock.setStExpYn(!stock.isStExpYn());
        stockRepository.save(stock);
        return stock.isStExpYn(); // 바뀐 값 프론트로 전달
    }

    // 사용 여부 수정 by 토글 버튼
    public boolean toggleUseYn(Long stId) {
        Stock stock = stockRepository.findById(stId).orElseThrow(() -> new NoSuchElementException("존재하지 않는 상품입니다."));
        stock.setStUseYn(!stock.isStUseYn());
        stock.setStExpYn(false);
        stockRepository.save(stock);
        return stock.isStUseYn(); // 바뀐 값 프론트로 전달
    }

    // 상품 삭제
    public void deleteStock(Long stId) {
        Stock stock = stockRepository.findById(stId).orElseThrow(() -> new NoSuchElementException("존재하지 않는 상품입니다."));
        stock.setStDelYn(true);
        stock.setStUseYn(false);
        stock.setStExpYn(false);
        stock.setStUpdDt(LocalDateTime.now());
        stockRepository.save(stock);
    }
    // 상품 정보 수정
    public void updateStock(Long stId, AdminStockDto adminStockDto) {
        Stock stock = stockRepository.findById(stId).orElseThrow(() -> new NoSuchElementException("존재하지 않는 상품입니다."));
        stock.setStBrNm(adminStockDto.getStBrNm());
        stock.setStCatNm(adminStockDto.getStCatNm());
        stock.setStNm(adminStockDto.getStNm());
        stock.setStPr(adminStockDto.getStPr());
        stock.setStCur(adminStockDto.getStCur());
        stock.setStImgUrl(adminStockDto.getStImgUrl());
        stock.setStReqYn(adminStockDto.isStReqYn());
        stock.setStReqMemId(adminStockDto.getStReqMemId());
        stock.setStUpdDt(LocalDateTime.now());
        // stId, stCreDt, stCraDt, stWisCnt, stPurCnt 등은 건드리지 않음
        stockRepository.save(stock);
    }

    // stock 객체를 AdminStockDto로 매핑
    private AdminStockDto mapToAdminStockDto(Stock stock) {
        return AdminStockDto.builder()
                .stId(stock.getStId())
                .stCd(stock.getStCd())
                .brId(stock.getBrId())
                .stBrNm(stock.getStBrNm())
                .catId(stock.getCatId())
                .stCatNm(stock.getStCatNm())
                .stNm(stock.getStNm())
                .stPr(stock.getStPr())
                .stCur(stock.getStCur())
                .stImgUrl(stock.getStImgUrl())
                .stExpYn(stock.isStExpYn())
                .stUseYn(stock.isStUseYn())
                .stDelYn(stock.isStDelYn())
                .stReqYn(stock.isStReqYn())
                .stReqMemId(stock.getStReqMemId())
                .stCraDt(stock.getStCraDt())
                .stCreDt(stock.getStCreDt())
                .stUpdDt(stock.getStUpdDt())
                .stWisCnt(stock.getStWisCnt())
                .stPurCnt(stock.getStPurCnt())
                .build();
    }
}
