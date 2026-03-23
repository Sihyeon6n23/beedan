package com.goodee.beedan.service.stock;

import com.goodee.beedan.dto.stock.StockListDto;
import com.goodee.beedan.entity.Brand;
import com.goodee.beedan.entity.Category;
import com.goodee.beedan.entity.Stock;
import com.goodee.beedan.repository.brand.BrandRepository;
import com.goodee.beedan.repository.category.CategoryRepository;
import com.goodee.beedan.repository.stock.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
