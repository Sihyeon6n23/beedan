package com.goodee.beedan.controller.crawling;

import com.goodee.beedan.entity.Brand;
import com.goodee.beedan.entity.Category;
import com.goodee.beedan.entity.CrawlingUrl;
import com.goodee.beedan.repository.brand.BrandRepository;
import com.goodee.beedan.repository.category.CategoryRepository;
import com.goodee.beedan.repository.crawling.CrawlingUrlRepository;
import com.goodee.beedan.service.crawling.CrawlingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("admin/crawling")
public class CrawlingController {

    private final CrawlingService crawlingService;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final CrawlingUrlRepository crawlingUrlRepository;

    @GetMapping
    public String crawlingPage(Model model) {
        List<CrawlingUrl> urls = crawlingService.findAllUrls();
        List<Brand> brands = brandRepository.findAll();
        List<Category> categories = categoryRepository.findAll();

        Map<Long, String> brandMap = brands.stream()
                .collect(Collectors.toMap(Brand::getBrId, Brand::getBrNm));
        Map<Long, String> catMap = categories.stream()
                .collect(Collectors.toMap(Category::getCatId, Category::getCatNm));

        urls.sort(Comparator.comparing(u -> brandMap.getOrDefault(u.getBrId(), "")));
        model.addAttribute("urls", urls);
        model.addAttribute("brands", brands);
        model.addAttribute("categories", categories);
        model.addAttribute("brandMap", brandMap);
        model.addAttribute("catMap", catMap);
        return "admin/crawling";
    }

    @GetMapping("/check-type/{id}")
    @ResponseBody
    public Map<String, String> checkType(@PathVariable Long id) {
        CrawlingUrl url = crawlingUrlRepository.findById(id).orElseThrow();
        Map<String, String> result = new HashMap<>();
        result.put("type", crawlingService.isShopify(url.getUrlUrl()) ? "shopify" : "html");
        return result;
    }

    @PostMapping("/url")
    public String saveUrl(@RequestParam String urlUrl,
                          @RequestParam String brNm,
                          @RequestParam String catNm,
                          RedirectAttributes redirectAttributes) {
        Brand brand = brandRepository.findByBrNm(brNm)
                .orElseGet(() -> brandRepository.save(Brand.builder().brNm(brNm).build()));

        Long catId;
        if ("AI 자동 분류".equals(catNm)) {
            catId = 0L;
        } else {
            Category category = categoryRepository.findByCatNm(catNm)
                    .orElseGet(() -> categoryRepository.save(Category.builder().catNm(catNm).build()));
            catId = category.getCatId();
        }

        crawlingService.saveUrl(CrawlingUrl.builder()
                .urlUrl(urlUrl)
                .brId(brand.getBrId())
                .catId(catId)
                .build());
        redirectAttributes.addFlashAttribute("message", "URL이 등록되었습니다.");
        return "redirect:/admin/crawling";
    }

    @PostMapping("/run/{id}")
    public String runCrawl(@PathVariable Long id,
                           @RequestParam String selItem,
                           @RequestParam String selNm,
                           @RequestParam String selPr,
                           @RequestParam String selImg,
                           @RequestParam String currency,
                           RedirectAttributes redirectAttributes) {
        try {
            int count = crawlingService.crawl(id, selItem, selNm, selPr, selImg, currency);
            redirectAttributes.addFlashAttribute("message", count + "개 상품이 저장되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "크롤링 실패: " + e.getMessage());
        }
        return "redirect:/admin/crawling";
    }
}
