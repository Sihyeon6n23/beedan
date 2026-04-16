package com.goodee.beedan.controller.stock;

import com.goodee.beedan.dto.file.FileDto;
import com.goodee.beedan.dto.file.RefDto;
import com.goodee.beedan.dto.stock.AdminStockDto;
import com.goodee.beedan.entity.Brand;
import com.goodee.beedan.entity.Category;
import com.goodee.beedan.service.file.FileService;
import com.goodee.beedan.service.stock.AdminStockService;
import com.goodee.beedan.service.stock.StockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/stock")
@RequiredArgsConstructor
public class AdminStockApiController {
    private final AdminStockService adminStockService;
    private final StockService stockService;
    private final FileService fileService;

    @GetMapping("/list")
    public Page<AdminStockDto> list (
            @RequestParam(required = false) String keyword
            , @RequestParam(required = false) String category
            , @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)LocalDate startDate
            , @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
            , @RequestParam(defaultValue = "0") int page) {
        LocalDateTime start = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime end = endDate != null ? endDate.atTime(23, 59, 59) : null;
        Pageable pageable = PageRequest.of(page, 20, Sort.by(Sort.Direction.DESC, "stId"));
        return adminStockService.findAdminStocks(keyword, category, start, end, pageable);
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
                                       @Valid @RequestBody AdminStockDto adminStockDto) {
        adminStockService.updateStock(stId, adminStockDto);
        return ResponseEntity.ok().build();
    }

    // 상품 이미지 업로드
    @PostMapping("/{stId}/image")
    public ResponseEntity<Map<String, String>> uploadImage(@PathVariable Long stId,
                                                           @RequestParam("file") MultipartFile file) throws IOException {
        RefDto refDto = RefDto.builder().refTy("STOCK").refNo(stId).build();
        List<FileDto> result = fileService.saveFile(List.of(file), refDto);
        FileDto saved = result.stream().filter(FileDto::isUploaded).findFirst().orElse(null);
        if (saved == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "파일 업로드 실패"));
        }
        String imgUrl = "/files/" + saved.getFilePat().replace("\\", "/")
                + "/" + saved.getFileUuid() + "." + saved.getFileExt();
        stockService.saveImgUrl(stId, imgUrl);
        return ResponseEntity.ok(Map.of("imgUrl", imgUrl));
    }

    // 상품 삭제
    @DeleteMapping("/{stId}")
    public ResponseEntity<Void> delete(@PathVariable Long stId) {
        adminStockService.deleteStock(stId);
        return ResponseEntity.ok().build();
    }

    // 일괄 카테고리 변경
    @PatchMapping("/batch-category")
    public ResponseEntity<Void> batchCategory(@RequestBody java.util.Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Integer> ids = (List<Integer>) body.get("stIds");
        String catNm = (String) body.get("catNm");
        List<Long> stIds = ids.stream().map(Integer::longValue).toList();
        adminStockService.batchUpdateCategory(stIds, catNm);
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
