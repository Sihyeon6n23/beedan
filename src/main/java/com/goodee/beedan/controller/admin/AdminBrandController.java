package com.goodee.beedan.controller.admin;

import com.goodee.beedan.entity.Brand;
import com.goodee.beedan.entity.Factory;
import com.goodee.beedan.repository.brand.BrandRepository;
import com.goodee.beedan.repository.quote.FactoryRepository;
import com.goodee.beedan.repository.quote.ShippingRateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/brand")
public class AdminBrandController {

    private final BrandRepository brandRepository;
    private final FactoryRepository factoryRepository;
    private final ShippingRateRepository shippingRateRepository;

    @GetMapping("/list")
    public String brandList(Model model) {
        List<Brand> brands = brandRepository.findAllByOrderByBrNmAsc();

        List<Map<String, Object>> brandList = new ArrayList<>();
        for (Brand brand : brands) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("brId", brand.getBrId());
            item.put("brNm", brand.getBrNm());

            List<Factory> factories = factoryRepository.findAllByBrIdAndFaYnTrue(brand.getBrId());
            item.put("factories", factories);
            item.put("factoryCount", factories.size());
            item.put("hasMapped", !factories.isEmpty());

            brandList.add(item);
        }

        model.addAttribute("brands", brandList);
        model.addAttribute("countryCodes", shippingRateRepository.findDistinctCountryCodes());
        return "admin/stock/brand-list";
    }

    // 공장 매핑 추가
    @PostMapping("/{brId}/factory")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addFactory(
            @PathVariable Long brId,
            @RequestBody Map<String, String> body) {
        Brand brand = brandRepository.findById(brId).orElse(null);
        if (brand == null) return ResponseEntity.badRequest().body(Map.of("status", "error", "message", "브랜드를 찾을 수 없습니다."));

        Factory factory = Factory.builder()
                .brId(brId)
                .faNm(body.get("faNm"))
                .faAd(body.get("faAd"))
                .faCty(body.get("faCty"))
                .faCCd(body.get("faCCd"))
                .build();
        factoryRepository.save(factory);

        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    // 공장 삭제 (비활성화)
    @DeleteMapping("/factory/{faId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteFactory(@PathVariable Long faId) {
        Factory factory = factoryRepository.findById(faId).orElse(null);
        if (factory == null) return ResponseEntity.badRequest().body(Map.of("status", "error", "message", "공장을 찾을 수 없습니다."));

        factory.deactivate();
        factoryRepository.save(factory);
        return ResponseEntity.ok(Map.of("status", "ok"));
    }
}
