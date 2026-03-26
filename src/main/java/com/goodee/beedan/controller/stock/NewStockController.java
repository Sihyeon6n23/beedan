package com.goodee.beedan.controller.stock;

import com.goodee.beedan.entity.Brand;
import com.goodee.beedan.entity.Category;
import com.goodee.beedan.entity.CrawlingUrl;
import com.goodee.beedan.repository.brand.BrandRepository;
import com.goodee.beedan.repository.category.CategoryRepository;
import com.goodee.beedan.repository.crawling.CrawlingUrlRepository;
import com.goodee.beedan.service.crawling.CrawlingService;
import com.goodee.beedan.service.stock.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.UUID;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/newstock")
public class NewStockController {

    private final CrawlingService crawlingService;
    private final StockService stockService;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final CrawlingUrlRepository crawlingUrlRepository;

    @Value("${upload.stock.dir:./uploads/stock}")
    private String uploadDir;

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
        return "admin/revenue/new-stock";
    }

    @GetMapping("/check-type/{id}")
    @ResponseBody
    public Map<String, String> checkType(@PathVariable Long id) {
        CrawlingUrl url = crawlingUrlRepository.findById(id).orElseThrow();
        boolean shopify = crawlingService.isShopify(url.getUrlUrl());
        boolean hasSel = url.getUrlCur() != null && !url.getUrlCur().isBlank();
        Map<String, String> result = new HashMap<>();
        result.put("type", shopify ? "shopify" : "html");
        result.put("hasSel", String.valueOf(hasSel));
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
        return "redirect:/admin/newstock";
    }

    // 자체 상품 등록 (수동)
    @PostMapping("/manual")
    public String saveManual(@RequestParam String stCd,
                             @RequestParam String stNm,
                             @RequestParam String brNm,
                             @RequestParam(required = false) String catNm,
                             @RequestParam BigDecimal stPr,
                             @RequestParam String stCur,
                             @RequestParam Long stReqMemId,
                             @RequestParam(required = false) MultipartFile imgFile,
                             RedirectAttributes redirectAttributes) {
        try {
            String imgUrl = null;
            if (imgFile != null && !imgFile.isEmpty()) {
                String ext = imgFile.getOriginalFilename() != null
                        ? imgFile.getOriginalFilename().substring(imgFile.getOriginalFilename().lastIndexOf('.'))
                        : ".jpg";
                String fileName = UUID.randomUUID() + ext;
                Path dir = Paths.get(uploadDir);
                Files.createDirectories(dir);
                Files.copy(imgFile.getInputStream(), dir.resolve(fileName));
                imgUrl = "/stock/" + fileName;
            }

            stockService.saveManual(stCd, stNm, brNm, catNm, stPr, stCur, imgUrl, stReqMemId);
            redirectAttributes.addFlashAttribute("message", "상품이 등록되었습니다.");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "이미지 업로드 실패: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "상품 등록 실패: " + e.getMessage());
        }
        redirectAttributes.addFlashAttribute("activeTab", "manual");
        return "redirect:/admin/newstock";
    }

    @PostMapping("/run/{id}")
    public String runCrawl(@PathVariable Long id,
                           @RequestParam(required = false) String selItem,
                           @RequestParam(required = false) String selNm,
                           @RequestParam(required = false) String selPr,
                           @RequestParam(required = false) String selImg,
                           @RequestParam(required = false) String currency,
                           RedirectAttributes redirectAttributes) {
        try {
            CrawlingUrl url = crawlingUrlRepository.findById(id).orElseThrow();

            // 셀렉터가 새로 입력된 경우 저장
            if (currency != null && !currency.isBlank()) {
                url.setUrlSelItem(selItem);
                url.setUrlSelNm(selNm);
                url.setUrlSelPr(selPr);
                url.setUrlSelImg(selImg);
                url.setUrlCur(currency);
                crawlingUrlRepository.save(url);
            }

            // 저장된 셀렉터 사용
            String effectiveSel  = url.getUrlSelItem();
            String effectiveNm   = url.getUrlSelNm();
            String effectivePr   = url.getUrlSelPr();
            String effectiveImg  = url.getUrlSelImg();
            String effectiveCur  = url.getUrlCur();

            int count = crawlingService.crawl(id, effectiveSel, effectiveNm, effectivePr, effectiveImg, effectiveCur);
            redirectAttributes.addFlashAttribute("message", count + "개 상품이 저장되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "크롤링 실패: " + e.getMessage());
        }
        return "redirect:/admin/newstock";
    }

}
