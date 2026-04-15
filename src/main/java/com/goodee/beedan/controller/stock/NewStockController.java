package com.goodee.beedan.controller.stock;

import com.goodee.beedan.config.security.MemberUserDetails;
import com.goodee.beedan.dto.crawling.SelectorForm;
import com.goodee.beedan.dto.crawling.UrlForm;
import com.goodee.beedan.dto.file.FileDto;
import com.goodee.beedan.dto.file.RefDto;
import com.goodee.beedan.dto.stock.NewStockForm;
import com.goodee.beedan.entity.Brand;
import com.goodee.beedan.entity.Category;
import com.goodee.beedan.entity.CrawlingUrl;
import com.goodee.beedan.entity.Stock;
import com.goodee.beedan.repository.brand.BrandRepository;
import com.goodee.beedan.repository.category.CategoryRepository;
import com.goodee.beedan.repository.crawling.CrawlingUrlRepository;
import com.goodee.beedan.service.crawling.CrawlingService;
import com.goodee.beedan.service.file.FileService;
import com.goodee.beedan.service.stock.StockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
import java.util.*;

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
    private final com.goodee.beedan.service.root.SchedulerService schedulerService;
    private final FileService fileService;

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
        model.addAttribute("lastCrawlTime", schedulerService.getSchedulerSetting().getLastCrawlingRunTime());
        return "admin/stock/new-stock";
    }

    @GetMapping("/check-type/{id}")
    @ResponseBody
    public Map<String, String> checkType(@PathVariable Long id) {
        CrawlingUrl url = crawlingUrlRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 URL입니다."));
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

    // URL 등록
    @PostMapping("/url")
    public String saveUrl(@Valid UrlForm urlForm,
                          RedirectAttributes redirectAttributes) {
        try {
            String resultMessage = crawlingService.registerNewUrl(urlForm);
            redirectAttributes.addFlashAttribute("message", resultMessage);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", "등록 실패: " + e.getMessage());
        }
        return "redirect:/admin/newstock";
    }

    // 자체 상품 등록 (수동)
    @PostMapping("/manual")
    public String saveManual(NewStockForm newStockForm,
                             RedirectAttributes redirectAttributes,
                             @AuthenticationPrincipal MemberUserDetails userDetails) {

        Long lastId = stockService.saveManual(newStockForm);

        List<MultipartFile> files = newStockForm.getNewFiles();
        boolean hasFile = files != null && !files.isEmpty()
                && files.stream().anyMatch(f -> f != null && !f.isEmpty());

        if (hasFile) {
            RefDto refDto = RefDto.builder()
                    .refTy("STOCK")
                    .refNo(lastId)
                    .build();
            List<FileDto> file;
            try {
                file = fileService.saveFile(files, refDto);
            } catch (IOException e) {
                throw new RuntimeException("파일 저장 실패: " + e.getMessage());
            }
            FileDto saved = file.stream()
                    .filter(FileDto::isUploaded)
                    .findFirst()
                    .orElse(null);
            if (saved != null) {
                String imgUrl = "/files/" + saved.getFilePat().replace("\\", "/")
                        + "/" + saved.getFileUuid() + "." + saved.getFileExt();
                stockService.saveImgUrl(lastId, imgUrl);
            }
        }

        redirectAttributes.addFlashAttribute("activeTab", "manual");
        return "redirect:/admin/newstock";
    }

    // 전체 크롤링 (urlAtYn=true 대상)
    @PostMapping("/run-all")
    @ResponseBody
    public Map<String, Object> runAllCrawl() {
        int count = crawlingService.crawlAll();
        return Map.of("success", true, "newCount", count);
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
                                 @Valid SelectorForm dto,
                                 RedirectAttributes redirectAttributes) {
        try {
            crawlingService.updateSelector(id, dto);
            redirectAttributes.addFlashAttribute("message", "셀렉터가 수정되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "셀렉터 수정 실패: " + e.getMessage());
        }
        return "redirect:/admin/newstock";
    }

    // URL 삭제
    @PostMapping("/delete/{id}")
    @ResponseBody
    public Map<String, String> deleteUrl(@PathVariable Long id) {
        crawlingService.deleteUrl(id);
        return Map.of("status", "ok");
    }

    // 자동 포함 토글 (urlUseYn)
//    @PostMapping("/toggle-use/{id}")
//    @ResponseBody
//    public Map<String, String> toggleUseYn(@PathVariable Long id,
//                                           @RequestBody Map<String, Boolean> body) {
//        CrawlingUrl url = crawlingUrlRepository.findById(id).orElseThrow();
//        url.setUrlUseYn(body.get("useYn"));
//        crawlingUrlRepository.save(url);
//        Map<String, String> result = new HashMap<>();
//        result.put("status", "ok");
//        return result;
//    }
}
