package com.goodee.beedan.service.stock;

import com.goodee.beedan.dto.stock.StockListDto;
import com.goodee.beedan.entity.Brand;
import com.goodee.beedan.entity.Category;
import com.goodee.beedan.entity.Stock;
import com.goodee.beedan.repository.brand.BrandRepository;
import com.goodee.beedan.repository.category.CategoryRepository;
import com.goodee.beedan.repository.stock.StockRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockService {
    private final StockRepository stockRepository;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;

    // 전체 상품 불러오기
    public Page<StockListDto> findAllStocks(Pageable pageable) {
        return stockRepository.findByStExpYnTrue(pageable)
                .map(this::mapToStockListDto);
    }

    // 전체 브랜드 목록 불러오기
    public List<Brand> findAllBrands() {
        return brandRepository.findAll();
    }

    // 전체 카테고리 목록 불러오기
    public List<Category> findAllCategories() {
        return categoryRepository.findAll();
    }

    // 필터 + 정렬 조회
    public Page<StockListDto> findFiltered(List<Long> brandIds, List<String> catNms, String sort, int page) {
        Sort sorting = switch (sort != null ? sort : "recent") {
            case "popularity" -> Sort.by(Sort.Direction.DESC, "stWisCnt");
            case "price-asc" -> Sort.by(Sort.Direction.ASC, "stPr");
            case "price-desc" -> Sort.by(Sort.Direction.DESC, "stPr");
            case "most-purchased" -> Sort.by(Sort.Direction.DESC, "stPurCnt");
            default -> Sort.by(Sort.Direction.DESC, "stId");
        };

        Pageable pageable = PageRequest.of(page, 8, sorting);

        Specification<Stock> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isTrue(root.get("stExpYn")));
            if (brandIds != null && !brandIds.isEmpty()) {
                predicates.add(root.get("brId").in(brandIds));
            }
            if (catNms != null && !catNms.isEmpty()) {
                predicates.add(root.get("stCatNm").in(catNms));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return stockRepository.findAll(spec, pageable).map(this::mapToStockListDto);
    }


    // 요청 상품 등록 (수동)
    @Transactional
    public void saveManual(String stCd, String stNm, String brNm, String catNm,
                           BigDecimal stPr, String stCur, String stImgUrl, Long stReqMemId) {
        Brand brand = brandRepository.findByBrNm(brNm)
                .orElseGet(() -> brandRepository.save(Brand.builder().brNm(brNm).build()));

        String catId = null;
        String catNmFinal = null;
        if (catNm != null && !catNm.isBlank()) {
            Category category = categoryRepository.findByCatNm(catNm)
                    .orElseGet(() -> categoryRepository.save(Category.builder().catNm(catNm).build()));
            catId = String.valueOf(category.getCatId());
            catNmFinal = category.getCatNm();
        }

        stockRepository.save(Stock.builder()
                .stCd(stCd)
                .stNm(stNm)
                .brId(brand.getBrId())
                .stBrNm(brand.getBrNm())
                .stCat(catId)
                .stCatNm(catNmFinal)
                .stPr(stPr)
                .stCur(stCur)
                .stImgUrl(stImgUrl)
                .stReqMemId(stReqMemId)
                .stExpYn(true)
                .stUseYn(true)
                .stDelYn(false)
                .stReqYn(true)
                .stCraDt(null)
                .stCreDt(LocalDateTime.now())
                .stUpdDt(null)
                .stWisCnt(null)
                .stPurCnt(null)
                .build());
    }

    // STOCK 엔티티 객체를 DTO 객체로 변환
    public StockListDto mapToStockListDto(Stock stock) {
        return StockListDto.builder()
                .stId(stock.getStId())
                .stBrNm(stock.getStBrNm())
                .stNm(stock.getStNm())
                .stCatNm(stock.getStCatNm())
                .stPr(stock.getStPr())
                .stCur(stock.getStCur())
                .stImgUrl(stock.getStImgUrl())
                .build();
    }
}
