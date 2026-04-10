package com.goodee.beedan.controller.admin;

import com.goodee.beedan.entity.Category;
import com.goodee.beedan.entity.HsCode;
import com.goodee.beedan.repository.category.CategoryRepository;
import com.goodee.beedan.repository.quote.HsCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/category")
public class AdminCategoryController {

    private final CategoryRepository categoryRepository;
    private final HsCodeRepository hsCodeRepository;

    @GetMapping("/list")
    public String categoryList(Model model) {
        List<Category> categories = categoryRepository.findAll();
        categories.sort(Comparator.comparing(Category::getCatNm));

        List<Map<String, Object>> catList = new ArrayList<>();
        for (Category cat : categories) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("catId", cat.getCatId());
            item.put("catNm", cat.getCatNm());

            HsCode hs = hsCodeRepository.findByCatId(cat.getCatId()).orElse(null);
            item.put("hsCode", hs);
            item.put("hasMapped", hs != null);

            catList.add(item);
        }

        model.addAttribute("categories", catList);
        return "admin/stock/category-list";
    }

    // HS 코드 매핑 추가/수정
    @PostMapping("/{catId}/hscode")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> saveHsCode(
            @PathVariable Long catId,
            @RequestBody Map<String, String> body) {
        Category cat = categoryRepository.findById(catId).orElse(null);
        if (cat == null) return ResponseEntity.badRequest().body(Map.of("status", "error", "message", "카테고리를 찾을 수 없습니다."));

        String hsCd = body.get("hsCd");
        String hsNm = body.get("hsNm");
        String hsDuRaStr = body.get("hsDuRa");
        String des = body.get("des");

        if (hsCd == null || hsCd.isBlank() || hsNm == null || hsNm.isBlank() || hsDuRaStr == null) {
            return ResponseEntity.badRequest().body(Map.of("status", "error", "message", "HS 코드, 품명, 관세율은 필수입니다."));
        }

        BigDecimal hsDuRa;
        try {
            hsDuRa = new BigDecimal(hsDuRaStr);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(Map.of("status", "error", "message", "관세율 형식이 올바르지 않습니다."));
        }

        HsCode existing = hsCodeRepository.findByCatId(catId).orElse(null);
        if (existing != null) {
            existing.updateCode(hsCd, hsNm, hsDuRa, des);
            hsCodeRepository.save(existing);
        } else {
            HsCode newHs = HsCode.builder()
                    .categoryId(catId)
                    .code(hsCd)
                    .name(hsNm)
                    .dutyRate(hsDuRa)
                    .description(des)
                    .build();
            hsCodeRepository.save(newHs);
        }

        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    // HS 코드 삭제
    @DeleteMapping("/{catId}/hscode")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteHsCode(@PathVariable Long catId) {
        HsCode hs = hsCodeRepository.findByCatId(catId).orElse(null);
        if (hs == null) return ResponseEntity.badRequest().body(Map.of("status", "error", "message", "HS 코드가 없습니다."));
        hsCodeRepository.delete(hs);
        return ResponseEntity.ok(Map.of("status", "ok"));
    }
}
