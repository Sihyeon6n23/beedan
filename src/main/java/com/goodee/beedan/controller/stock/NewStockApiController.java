package com.goodee.beedan.controller.stock;

import com.goodee.beedan.config.security.MemberUserDetails;
import com.goodee.beedan.dto.stock.StockListDto;
import com.goodee.beedan.service.crawling.CrawlingService;
import com.goodee.beedan.service.stock.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/newstock")
@RequiredArgsConstructor
public class NewStockApiController {

    private final CrawlingService crawlingService;

    @PostMapping("/autoyn/{urlId}")
    public ResponseEntity<Void> autoMode(@PathVariable Long urlId) {
        crawlingService.autoYnChange(urlId);
        return ResponseEntity.ok().build();
    }
}
