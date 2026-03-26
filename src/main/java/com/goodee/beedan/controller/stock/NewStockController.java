package com.goodee.beedan.controller.stock;

import com.goodee.beedan.dto.crawling.SelectorForm;
import com.goodee.beedan.dto.crawling.UrlForm;
import com.goodee.beedan.dto.stock.NewStockForm;
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

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

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
    public String crawlingPage(Model model,
                               @ModelAttribute("url") UrlForm urlForm) {
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
        return "admin/stock/new-stock";
    }

    @GetMapping("/check-type/{id}")
    @ResponseBody
    public Map<String, String> checkType(@PathVariable Long id) {
        CrawlingUrl url = crawlingUrlRepository.findById(id).orElseThrow();
        String urlTy = url.getUrlTy();

        String type;

        if (urlTy != null) {
            type = "SHOPIFY".equals(urlTy) ? "shopify" : "html";
        } else {
            // 첫 크롤링 — 타입 감지 + DB 저장
            boolean shopify = crawlingService.isShopify(url.getUrlUrl());
            type = shopify ? "shopify" : "html";
            url.setUrlTy(shopify ? "SHOPIFY" : null);
            if (shopify) crawlingUrlRepository.save(url);
        }

        Map<String, String> result = new HashMap<>();
        result.put("type", type);
        return result;
    }

    @PostMapping("/url")
    public String saveUrl(UrlForm urlForm,
                          RedirectAttributes redirectAttributes) {
        Brand brand = brandRepository.findByBrNm(urlForm.getBrNm())
                .orElseGet(() -> brandRepository.save(Brand.builder().brNm(urlForm.getBrNm()).build()));

        Long catId;
        if ("AI 자동 분류".equals(urlForm.getCatNm())) {
            catId = 0L;
        } else {
            Category category = categoryRepository.findByCatNm(urlForm.getCatNm())
                    .orElseGet(() -> categoryRepository.save(Category.builder().catNm(urlForm.getCatNm()).build()));
            catId = category.getCatId();
        }

        crawlingService.saveUrl(
                CrawlingUrl.builder()
                .urlUrl(urlForm.getUrlUrl())
                .brId(brand.getBrId())
                .catId(catId)
                .urlUseYn(TRUE)
                .urlDelYn(FALSE)
                .urlAtYn(TRUE)
                .build());
        redirectAttributes.addFlashAttribute("message", "URL이 등록되었습니다.");
        return "redirect:/admin/newstock";
    }

    // 자체 상품 등록 (수동)
    @PostMapping("/manual")
    public String saveManual(NewStockForm newStockForm,
                             RedirectAttributes redirectAttributes) {
        try {
            String imgUrl = null;
            MultipartFile imgFile = newStockForm.getImgFile();
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

            stockService.saveManual(newStockForm, imgUrl);
            redirectAttributes.addFlashAttribute("message", "상품이 등록되었습니다.");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "이미지 업로드 실패: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "상품 등록 실패: " + e.getMessage());
        }
        redirectAttributes.addFlashAttribute("activeTab", "manual");
        return "redirect:/admin/newstock";
    }

    // 크롤링
    @PostMapping("/run/{id}")
    public String runCrawl(@PathVariable Long id,
                           RedirectAttributes redirectAttributes) {
        try {
            int count = crawlingService.crawl(id);
            redirectAttributes.addFlashAttribute("message", count + "개 상품이 저장되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "크롤링 실패: " + e.getMessage());
        }
        return "redirect:/admin/newstock";
    }

    // 셀렉터 수정
    @PostMapping("/selector/{id}")
    public String updateSelector(@PathVariable Long id,
                                 SelectorForm dto,
                                 RedirectAttributes redirectAttributes) {
        try {
            crawlingService.updateSelector(id, dto);
            redirectAttributes.addFlashAttribute("message", "셀렉터가 수정되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "셀렉터 수정 실패: " + e.getMessage());
        }
        return "redirect:/admin/newstock";
    }

    // 자동 포함 토글 (urlUseYn)
    @PostMapping("/toggle-use/{id}")
    @ResponseBody
    public Map<String, String> toggleUseYn(@PathVariable Long id,
                                           @RequestBody Map<String, Boolean> body) {
        CrawlingUrl url = crawlingUrlRepository.findById(id).orElseThrow();
        url.setUrlUseYn(body.get("useYn"));
        crawlingUrlRepository.save(url);
        Map<String, String> result = new HashMap<>();
        result.put("status", "ok");
        return result;
    }
}
