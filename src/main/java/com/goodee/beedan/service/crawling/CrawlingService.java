package com.goodee.beedan.service.crawling;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.goodee.beedan.entity.Brand;
import com.goodee.beedan.entity.Category;
import com.goodee.beedan.entity.CrawlingUrl;
import com.goodee.beedan.entity.Stock;
import com.goodee.beedan.repository.brand.BrandRepository;
import com.goodee.beedan.repository.category.CategoryRepository;
import com.goodee.beedan.repository.crawling.CrawlingUrlRepository;
import com.goodee.beedan.repository.stock.StockRepository;
import lombok.RequiredArgsConstructor;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goodee.beedan.dto.crawling.SelectorForm;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CrawlingService {

    private final CrawlingUrlRepository crawlingUrlRepository;
    private final StockRepository stockRepository;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final AiCategoryService aiCategoryService;

    private static final Long AI_CATEGORY_ID = 0L;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public void autoYnChange(Long urlId) {
        CrawlingUrl crawlingUrl = crawlingUrlRepository.findById(urlId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 URL입니다."));
        crawlingUrl.setUrlAtYn(!crawlingUrl.isUrlAtYn());
    }

    private record RawProduct(String name, BigDecimal price, String imgUrl) {}

    public List<CrawlingUrl> findAllUrls() {

        return crawlingUrlRepository.findByUrlDelYnFalse();
    }

    @Transactional
    public void saveUrl(CrawlingUrl crawlingUrl) {

        crawlingUrlRepository.save(crawlingUrl);
    }

    @Transactional
    public void updateSelector(Long urlId, SelectorForm dto) {
        CrawlingUrl url = crawlingUrlRepository.findById(urlId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 URL입니다."));
        url.setUrlSelItem(dto.getSelItem());
        url.setUrlSelNm(dto.getSelNm());
        url.setUrlSelPr(dto.getSelPr());
        url.setUrlSelImg(dto.getSelImg());
        url.setUrlCur(dto.getCurrency());
    }


    @Transactional
    public int crawl(Long urlId) throws IOException {
        CrawlingUrl crawlingUrl = crawlingUrlRepository.findById(urlId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 URL입니다."));

        String selItem  = crawlingUrl.getUrlSelItem();
        String selNm    = crawlingUrl.getUrlSelNm();
        String selPr    = crawlingUrl.getUrlSelPr();
        String selImg   = crawlingUrl.getUrlSelImg();
        String currency = crawlingUrl.getUrlCur();

        boolean aiMode = AI_CATEGORY_ID.equals(crawlingUrl.getCatId());

        Brand brand = brandRepository.findById(crawlingUrl.getBrId()).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 브랜드입니다."));
        Category fixedCategory = aiMode ? null : categoryRepository.findById(crawlingUrl.getCatId()).orElse(null);

        // 1단계: 저장된 방식이 있으면 먼저 시도, 없거나 실패 시 폴백
        String url = crawlingUrl.getUrlUrl();
        String method = crawlingUrl.getUrlTy();
        List<RawProduct> rawList = null;
        String usedMethod = null;

        if ("SHOPIFY".equals(method)) {
            rawList = tryShopifyJson(url);
            if (rawList != null) usedMethod = "SHOPIFY";
        } else if ("JSOUP".equals(method)) {
            rawList = crawlHtml(url, selItem, selNm, selPr, selImg);
            if (!rawList.isEmpty()) usedMethod = "JSOUP";
        } else if ("PLAYWRIGHT".equals(method)) {
            rawList = crawlWithPlaywright(url, selItem, selNm, selPr, selImg);
            if (!rawList.isEmpty()) usedMethod = "PLAYWRIGHT";
        }

        // 저장된 방식 실패 또는 없으면 → 기존 폴백
        if (usedMethod == null) {
            rawList = tryShopifyJson(url);
            if (rawList != null) {
                usedMethod = "SHOPIFY";
            } else {
                rawList = crawlHtml(url, selItem, selNm, selPr, selImg);
                if (!rawList.isEmpty()) {
                    usedMethod = "JSOUP";
                } else {
                    rawList = crawlWithPlaywright(url, selItem, selNm, selPr, selImg);
                    if (!rawList.isEmpty()) usedMethod = "PLAYWRIGHT";
                }
            }
        }

        // 2단계: AI 자동 분류
        Map<String, String> aiCategoryMap = Map.of();
        if (aiMode && !rawList.isEmpty()) {
            List<String> categoryNames = categoryRepository.findAll()
                    .stream().map(Category::getCatNm).toList();
            List<String> productNames = rawList.stream().map(RawProduct::name).toList();
            aiCategoryMap = aiCategoryService.categorize(productNames, categoryNames);
        }

        // 3단계: Stock upsert (브랜드+상품명 기준 중복 체크)
        String brNm = brand != null ? brand.getBrNm() : "UNK";
        String prefix = brNm.replaceAll("[^a-zA-Z]", "").toUpperCase();
        prefix = prefix.length() >= 3 ? prefix.substring(0, 3) : String.format("%-3s", prefix).replace(' ', 'X');
        long existingCount = stockRepository.countByBrId(crawlingUrl.getBrId());

        // 등록하려는 브랜드의
        List<Stock> existingStocks = stockRepository.findByBrId(crawlingUrl.getBrId());

        // 새로운 브랜드면 empty, 기존 브랜드면 set으로 담음
        // 삼항 연산자나 Stream으로 안전하게 Set 생성
        Set<String> existingNames = existingStocks.stream()
                .map(s -> s.getStNm().trim())
                .collect(Collectors.toSet());

        // 현재 DB에 있는 해당 브랜드 상품 총 개수 (코드 생성용)
        long currentTotalCount = existingStocks.size();

        int newCount = 0;
        int addedInThisLoop = 0; // 이번 루프에서 추가된 개수 카운트

        for (RawProduct raw : rawList) {
            // DB 안 가고 메모리에서 즉시 비교!
            if (existingNames.contains(raw.name())) {
                continue;
            }

            // 신규 상품 등록
            String catNm;
            Long catId;
            if (aiMode) {
                catNm = aiCategoryMap.getOrDefault(raw.name(), "");
                Category aiCat = categoryRepository.findByCatNm(catNm).orElse(null);
                if (aiCat == null) {
                    catNm = "ETC";
                    aiCat = categoryRepository.findByCatNm("ETC")
                            .orElseGet(() -> categoryRepository.save(
                                    Category.builder().catNm("ETC").build()));
                }
                catId = aiCat.getCatId();
            } else {
                catNm = fixedCategory != null ? fixedCategory.getCatNm() : "";
                catId = fixedCategory != null ? fixedCategory.getCatId() : null;
            }

                String stCd = prefix + String.format("%05d", currentTotalCount + (++addedInThisLoop));
                stockRepository.save(Stock.builder()
                        .stCd(stCd)
                        .brId(crawlingUrl.getBrId())
                        .stBrNm(brand != null ? brand.getBrNm() : "")
                        .catId(catId)
                        .stCatNm(catNm)
                        .stNm(raw.name())
                        .stPr(raw.price())
                        .stCur(currency)
                        .stImgUrl(raw.imgUrl())
                        .stExpYn(false)
                        .stUseYn(true)
                        .stDelYn(false)
                        .stReqYn(false)
                        .stWisCnt(0L)
                        .stPurCnt(0L)
                        .stCraDt(LocalDateTime.now())
                        .stCreDt(LocalDateTime.now())
                        .build());
                newCount++;
            }

        // 성공한 크롤링 방식 저장
        if (usedMethod != null) {
            crawlingUrl.setUrlTy(usedMethod);
            crawlingUrl.setUrlUpdDt(LocalDateTime.now());
        }

        return newCount;
    }

    // 전체 크롤링
    @Transactional
        public int crawlAll() {
        List<CrawlingUrl> targets = crawlingUrlRepository
                .findByUrlDelYnFalseAndUrlUseYnTrueAndUrlAtYnTrue();

        int totalNewCount = 0;

        for (CrawlingUrl url : targets) {
            try {
                int count = this.crawl(url.getUrlId());
                totalNewCount += count;
                log.info("전체 크롤링 - 완료: urlId={}, 신규={}건", url.getUrlId(), count);
            } catch (Exception e) {
                log.error("전체 크롤링 - 실패: urlId={}, 사유={}", url.getUrlId(), e.getMessage());
            }
        }

        log.info("전체 크롤링 종료: 대상={}개, 총 신규={}건", targets.size(), totalNewCount);
        return totalNewCount;
    }

    // Shopify JSON API 시도 (성공 시 상품 목록 반환, 실패 시 null)
    private List<RawProduct> tryShopifyJson(String url) {
        try {
            String base = url.split("\\?")[0].replaceAll("/+$", "");
            String jsonUrl = base.endsWith("/products.json") ? base : base + "/products.json";
            jsonUrl += "?limit=250";

            String body = Jsoup.connect(jsonUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .ignoreContentType(true)
                    .timeout(10_000)
                    .execute()
                    .body();
            JsonNode root = objectMapper.readTree(body);
            JsonNode products = root.path("products");
            if (!products.isArray() || products.isEmpty()) return null;

            List<RawProduct> list = new ArrayList<>();
            for (JsonNode p : products) {
                String name = p.path("title").asText();
                if (name.isBlank()) continue;

                String priceText = p.path("variants").path(0).path("price").asText("");
                BigDecimal price = BigDecimal.ZERO;
                try {
                    if (!priceText.isBlank()) price = new BigDecimal(priceText);
                } catch (NumberFormatException ignored) {}

                String imgUrl = p.path("images").path(0).path("src").asText("");
                list.add(new RawProduct(name, price, imgUrl));
            }
            return list.isEmpty() ? null : list;
        } catch (Exception e) {
            return null; // Shopify가 아니면 HTML 방식으로 폴백
        }
    }

    // Shopify 여부 판별 (외부에서 호출용)
    public boolean isShopify(String url) {

        return tryShopifyJson(url) != null;
    }

    // Playwright HTML 파싱 (JS 렌더링 사이트용)
    private List<RawProduct> crawlWithPlaywright(String url, String selItem, String selNm, String selPr, String selImg) {
        if (selItem == null || selItem.isBlank()) return List.of();
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(
                    new BrowserType.LaunchOptions()
                            .setHeadless(true)
                            .setChannel("chrome")
            );
            Page page = browser.newPage();
            page.navigate(url, new Page.NavigateOptions()
                    .setTimeout(30_000));
            page.waitForLoadState(com.microsoft.playwright.options.LoadState.NETWORKIDLE,
                    new Page.WaitForLoadStateOptions().setTimeout(15_000));

            String html = page.content();
            browser.close();

            Document doc = Jsoup.parse(html, url);  // base URL 전달 → abs:src 정상 동작
            Elements items = doc.select(selItem);

            log.info("[Playwright] 찾은 아이템 수: {}", + items.size());

            List<RawProduct> list = new ArrayList<>();
            for (Element item : items) {
                String name = item.select(selNm).text();
                if (name.isBlank()) continue;

                String priceText = item.select(selPr).text().replaceAll("[^0-9.]", "");
                String imgUrl = item.select(selImg).attr("abs:src");

                BigDecimal price = BigDecimal.ZERO;
                try {
                    if (!priceText.isBlank()) price = new BigDecimal(priceText);
                } catch (NumberFormatException ignored) {}

                list.add(new RawProduct(name, price, imgUrl));
            }
            return list;
        } catch (Exception e) {
            log.info("[Playwright] 오류: {}", e.getMessage());
            return List.of();
        }
    }

    // Jsoup HTML 파싱
    private List<RawProduct> crawlHtml(String url, String selItem, String selNm, String selPr, String selImg) throws IOException {
        if (selItem == null || selItem.isBlank()) return List.of();
        Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(10_000)
                .get();

        Elements items = doc.select(selItem);
        List<RawProduct> list = new ArrayList<>();

        for (Element item : items) {
            String name = item.select(selNm).text();
            if (name.isBlank()) continue;

            String priceText = item.select(selPr).text().replaceAll("[^0-9.]", "");
            String imgUrl = item.select(selImg).attr("abs:src");

            BigDecimal price = BigDecimal.ZERO;
            try {
                if (!priceText.isBlank()) price = new BigDecimal(priceText);
            } catch (NumberFormatException ignored) {}

            list.add(new RawProduct(name, price, imgUrl));
        }
        return list;
    }
}
