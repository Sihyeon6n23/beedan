package com.goodee.beedan.service.stock;

import com.goodee.beedan.dto.stock.NewStockForm;
import com.goodee.beedan.dto.stock.StockListDto;
import com.goodee.beedan.entity.*;
import com.goodee.beedan.repository.brand.BrandRepository;
import com.goodee.beedan.repository.category.CategoryRepository;
import com.goodee.beedan.repository.hitstock.HitStockRepository;
import com.goodee.beedan.repository.stock.StockRepository;
import com.goodee.beedan.repository.wishlist.WishlistRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class StockService {
    private final StockRepository stockRepository;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final WishlistRepository wishlistRepository;
    private final HitStockRepository hitStockRepository;

    // 상품 단건 조회
    public Stock findById(Long stId) {
        return stockRepository.findById(stId).orElseThrow(() -> new NoSuchElementException("존재하지 않는 상품입니다."));
    }

    // 찜 여부 확인
    public boolean isWished(Long stId, Long memId) {
        if (memId == null) return false;
        return wishlistRepository.findByStIdAndMemId(stId, memId) != null;
    }

    // 전체 상품 불러오기
    public Page<StockListDto> findAllStocks(Pageable pageable, Long memId) {
        Set<Long> wishedIds = memId != null
                ? wishlistRepository.findAllByMemId(memId).stream()
                .map(Wishlist::getStId).collect(Collectors.toSet())
                : Collections.emptySet();
        return stockRepository.findByStExpYnTrue(pageable)
                .map(stock -> mapToStockListDto(stock, wishedIds));
    }

    // wishlist 상품 불러오기
    public Page<StockListDto> findWishedItems(Pageable pageable, List<Wishlist> wishedItems) {
        List<Long> stIds = wishedItems.stream()
                .map(Wishlist::getStId)
                .collect(Collectors.toList());
        if (stIds.isEmpty()) {
            return Page.empty(pageable);
        }
        Set<Long> wishedIdSet = new HashSet<>(stIds);
        return stockRepository.findByStIdInAndStExpYnTrue(stIds, pageable)
                .map(stock -> mapToStockListDto(stock, wishedIdSet));
    }

    // 전체 브랜드 목록 불러오기
    public List<Brand> findAllBrands() {
        // 개별 요청 아이템만 존재하는 브랜드는 제외
        List<Long> activeBrandIds = stockRepository.findDistinctBrandIdsWithStock();
        return brandRepository.findAllById(activeBrandIds);
    }

    // 전체 카테고리 목록 불러오기
    public List<Category> findAllCategories() {
        return categoryRepository.findAll();
    }


    // 필터 + 정렬 조회
    public Page<StockListDto> findFiltered(List<Long> brandIds
                                         , List<String> catNms
                                         , String keyword
                                         , String sort
                                         , int page
                                         , Long memId) {
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
            predicates.add(cb.isFalse(root.get("stReqYn")));
            if (brandIds != null && !brandIds.isEmpty()) {
                predicates.add(root.get("brId").in(brandIds));
            }
            if (catNms != null && !catNms.isEmpty()) {
                predicates.add(root.get("stCatNm").in(catNms));
            }
            if (keyword != null && !keyword.isBlank()) {
                String pattern = "%" + keyword.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("stNm")), pattern),
                        cb.like(cb.lower(root.get("stBrNm")),pattern)
                        ));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Set<Long> wishedIds = memId != null
                ? wishlistRepository.findAllByMemId(memId).stream()
                .map(Wishlist::getStId).collect(Collectors.toSet())
                : Collections.emptySet();

        return stockRepository.findAll(spec, pageable)
                .map(stock -> mapToStockListDto(stock, wishedIds));
    }


    // wishlist 필터 + 정렬 조회
    public Page<StockListDto> findWishedFiltered(List<Long> brandIds
                                                , List<String> catNms
                                                , String keyword
                                                , String sort
                                                , int page
                                                , Long memId) {
        List<Long> wishedStIds = wishlistRepository.findAllByMemId(memId).stream()
                .map(Wishlist::getStId).collect(Collectors.toList());
        if (wishedStIds.isEmpty()) {
            return Page.empty();
        }
        Set<Long> wishedIdSet = new HashSet<>(wishedStIds);

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
            predicates.add(root.get("stId").in(wishedStIds));
            if (brandIds != null && !brandIds.isEmpty()) {
                predicates.add(root.get("brId").in(brandIds));
            }
            if (catNms != null && !catNms.isEmpty()) {
                predicates.add(root.get("stCatNm").in(catNms));
            }
            if (keyword != null && !keyword.isBlank()) {
                String pattern = "%" + keyword.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("stNm")), pattern),
                        cb.like(cb.lower(root.get("stBrNm")), pattern)
                ));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return stockRepository.findAll(spec, pageable)
                .map(stock -> mapToStockListDto(stock, wishedIdSet));
    }

    // 요청 상품 등록 (수동)
    @Transactional
    public Long saveManual(NewStockForm newStockForm) {
        Brand brand = brandRepository.findByBrNm(newStockForm.getBrNm())
                .orElseGet(() -> brandRepository.save(Brand.builder().brNm(newStockForm.getBrNm()).build()));

        Category category = categoryRepository.findByCatNm(newStockForm.getCatNm())
                .orElseGet(() -> categoryRepository.save(Category.builder().catNm(newStockForm.getCatNm()).build()));

        Stock save = stockRepository.save(Stock.builder()
                .stCd(newStockForm.getStCd())
                .stNm(newStockForm.getStNm())
                .brId(brand.getBrId())
                .stBrNm(brand.getBrNm())
                .catId(category.getCatId())
                .stCatNm(category.getCatNm())
                .stPr(newStockForm.getStPr())
                .stCur(newStockForm.getStCur())
                .stReqMemId(newStockForm.getStReqMemId())
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

        return save.getStId();

    }

    // 상품 구매시 날짜별 구매 현황 기록용
    public void stockHitRecord(Long stId) {
        Stock stock = stockRepository.findById(stId).orElseThrow(() -> new NoSuchElementException("존재하지 않는 상품입니다."));
        HitStock hitStock = hitStockRepository.save(HitStock.builder()
                .stId(stId)
                .hitDt(LocalDateTime.now())
                .build());
    }

    // 내 상품 조회 (stReqYn=true, stReqMemId=memId)
    public Page<StockListDto> findMyItems(List<Long> brandIds, List<String> catNms, String keyword, String sort, int page, Long memId) {
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
            predicates.add(cb.isTrue(root.get("stReqYn")));
            predicates.add(cb.equal(root.get("stReqMemId"), memId));
            if (brandIds != null && !brandIds.isEmpty()) {
                predicates.add(root.get("brId").in(brandIds));
            }
            if (catNms != null && !catNms.isEmpty()) {
                predicates.add(root.get("stCatNm").in(catNms));
            }
            if (keyword != null && !keyword.isBlank()) {
                String pattern = "%" + keyword.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("stNm")), pattern),
                        cb.like(cb.lower(root.get("stBrNm")), pattern)
                ));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Set<Long> wishedIds = memId != null
                ? wishlistRepository.findAllByMemId(memId).stream()
                .map(Wishlist::getStId).collect(Collectors.toSet())
                : Collections.emptySet();

        return stockRepository.findAll(spec, pageable)
                .map(stock -> mapToStockListDto(stock, wishedIds));
    }

    // 메인 화면 신상품 조회 (최근 30개 추출 후 랜덤 10개)
    public List<StockListDto> findNewStocks() {
        List<Stock> newStocks = stockRepository.findTop30ByStExpYnTrueOrderByStCraDtDesc();
        Collections.shuffle(newStocks);
        return newStocks.stream()
                .limit(16)
                .map(stock -> mapToStockListDto(stock, Collections.emptySet()))
                .collect(Collectors.toList());
    }

    // STOCK 엔티티 객체를 DTO 객체로 변환
    public StockListDto mapToStockListDto(Stock stock, Set<Long> wishedIds) {
        return StockListDto.builder()
                .stId(stock.getStId())
                .stBrNm(stock.getStBrNm())
                .stNm(stock.getStNm())
                .stCatNm(stock.getStCatNm())
                .stPr(stock.getStPr())
                .stCur(stock.getStCur())
                .stImgUrl(stock.getStImgUrl())
                .wished(wishedIds.contains(stock.getStId()))
                .build();
    }

    // 상품 이미지 URL 저장 (업로드 후 URL 저장용)
    public void saveImgUrl(Long lastId, String imgUrl) {
        Stock stock = stockRepository.findById(lastId).orElseThrow(() -> new NoSuchElementException("존재하지 않는 상품입니다."));
        stock.setStImgUrl(imgUrl);
        stockRepository.save(stock);
    }
}
