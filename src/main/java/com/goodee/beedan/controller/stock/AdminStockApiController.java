package com.goodee.beedan.controller.stock;

import com.goodee.beedan.config.security.MemberUserDetails;
import com.goodee.beedan.dto.stock.AdminStockDto;
import com.goodee.beedan.dto.stock.StockListDto;
import com.goodee.beedan.entity.Brand;
import com.goodee.beedan.entity.Category;
import com.goodee.beedan.service.crawling.CrawlingService;
import com.goodee.beedan.service.stock.AdminStockService;
import com.goodee.beedan.service.stock.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin/stock")
@RequiredArgsConstructor
public class AdminStockApiController {
    private final AdminStockService adminStockService;
    private final StockService stockService;

    @GetMapping("/list")
    public Page<AdminStockDto> list (
            @RequestParam(required = false) String keyword
            , @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)LocalDate startDate
            , @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
            , @RequestParam(defaultValue = "0") int page) {
        LocalDateTime start = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime end = endDate != null ? endDate.atTime(23, 59, 59) : null;
        Pageable pageable = PageRequest.of(page, 20, Sort.by(Sort.Direction.DESC, "stId"));
        return adminStockService.findAdminStocks(keyword, start, end, pageable);
    }

    // 노출 여부 반전
    @PatchMapping("/{stId}/toggle-exp")
    public ResponseEntity<Void> toggleExp(@PathVariable Long stId) {
        adminStockService.toggleExpYn(stId);
        return ResponseEntity.ok().build();
    }

    // 사용 여부 반전
    @PatchMapping("/{stId}/toggle-use")
    public ResponseEntity<Void> toggleUse(@PathVariable Long stId) {
        adminStockService.toggleUseYn(stId);
        return ResponseEntity.ok().build();
    }

    // 상품 정보 수정
    @PutMapping("/{stId}")
    public ResponseEntity<Void> update(@PathVariable Long stId,
                                       @RequestBody AdminStockDto adminStockDto) {
        adminStockService.updateStock(stId, adminStockDto);
        return ResponseEntity.ok().build();
    }

    // 상품 삭제
    @DeleteMapping("/{stId}")
    public ResponseEntity<Void> delete(@PathVariable Long stId) {
        adminStockService.deleteStock(stId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/brands")
    public List<Brand> getBrands() {
        return stockService.findAllBrands();
    }
    @GetMapping("/categories")
    public List<Category> getCategories() {
        return stockService.findAllCategories();
    }
}
