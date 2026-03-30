package com.goodee.beedan.controller.stock;

import com.goodee.beedan.config.security.MemberUserDetails;
import com.goodee.beedan.dto.stock.StockListDto;
import com.goodee.beedan.service.stock.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stock")
@RequiredArgsConstructor
public class StockApiController {

    private final StockService stockService;

    @GetMapping("/list")
    public Page<StockListDto> list(
            @RequestParam(required = false) List<Long> brands,
            @RequestParam(required = false) List<String> categories,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "popularity") String sort,
            @RequestParam(defaultValue = "0") int page,
            @AuthenticationPrincipal MemberUserDetails userDetails) {
        Long memId = userDetails != null ? userDetails.getMemberId() : null;
        return stockService.findFiltered(brands, categories, keyword, sort, page, memId);
    }
}
