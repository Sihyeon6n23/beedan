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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CrawlingService {

    private final CrawlingUrlRepository crawlingUrlRepository;
    private final StockRepository stockRepository;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final AiCategoryService aiCategoryService;

    private static final Long AI_CATEGORY_ID = 0L;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private record RawProduct(String name, BigDecimal price, String imgUrl) {}

    public List<CrawlingUrl> findAllUrls() {
        return crawlingUrlRepository.findAll();
    }

    @Transactional
    public void saveUrl(CrawlingUrl crawlingUrl) {
        crawlingUrlRepository.save(crawlingUrl);
    }

    @Transactional
    public int crawl(Long urlId, String selItem, String selNm, String selPr, String selImg, String currency) throws IOException {
        CrawlingUrl crawlingUrl = crawlingUrlRepository.findById(urlId)
                .orElseThrow(() -> new IllegalArgumentException("URL not found: " + urlId));

        boolean aiMode = AI_CATEGORY_ID.equals(crawlingUrl.getCatId());

        Brand brand = brandRepository.findById(crawlingUrl.getBrId()).orElse(null);
        Category fixedCategory = aiMode ? null : categoryRepository.findById(crawlingUrl.getCatId()).orElse(null);

        // 1단계: Shopify JSON 시도 → 실패 시 HTML 폴백
        List<RawProduct> rawList = tryShopifyJson(crawlingUrl.getUrlUrl());
        if (rawList == null) {
            rawList = crawlHtml(crawlingUrl.getUrlUrl(), selItem, selNm, selPr, selImg);
        }

        // 2단계: AI 자동 분류
        Map<String, String> aiCategoryMap = Map.of();
        if (aiMode && !rawList.isEmpty()) {
            List<String> categoryNames = categoryRepository.findAll()
                    .stream().map(Category::getCatNm).toList();
            List<String> productNames = rawList.stream().map(RawProduct::name).toList();
            aiCategoryMap = aiCategoryService.categorize(productNames, categoryNames);
        }

        // 3단계: Stock 엔티티 빌드
        String brNm = brand != null ? brand.getBrNm() : "UNK";
        String prefix = brNm.replaceAll("[^a-zA-Z]", "").toUpperCase();
        prefix = prefix.length() >= 3 ? prefix.substring(0, 3) : String.format("%-3s", prefix).replace(' ', 'X');
        long existingCount = stockRepository.countByBrId(crawlingUrl.getBrId());

        List<Stock> stocks = new ArrayList<>();
        int i = 0;
        for (RawProduct raw : rawList) {
            String catNm;
            String catId;
            if (aiMode) {
                catNm = aiCategoryMap.getOrDefault(raw.name(), "");
                Category aiCat = categoryRepository.findByCatNm(catNm).orElse(null);
                catId = aiCat != null ? String.valueOf(aiCat.getCatId()) : "";
            } else {
                catNm = fixedCategory != null ? fixedCategory.getCatNm() : "";
                catId = fixedCategory != null ? String.valueOf(fixedCategory.getCatId()) : "";
            }

            String stCd = prefix + String.format("%05d", existingCount + (++i));

            stocks.add(Stock.builder()
                    .stCd(stCd)
                    .brId(crawlingUrl.getBrId())
                    .stBrNm(brand != null ? brand.getBrNm() : "")
                    .stCat(catId)
                    .stCatNm(catNm)
                    .stNm(raw.name())
                    .stPr(raw.price())
                    .stCur(currency)
                    .stImgUrl(raw.imgUrl())
                    .stExpYn(true)
                    .stUseYn(true)
                    .stDelYn(false)
                    .stReqYn(false)
                    .stWisCnt(0L)
                    .stPurCnt(0L)
                    .stCraDt(LocalDateTime.now())
                    .stCreDt(LocalDateTime.now())
                    .stUpdDt(LocalDateTime.now())
                    .build());
        }

        stockRepository.saveAll(stocks);
        return stocks.size();
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
