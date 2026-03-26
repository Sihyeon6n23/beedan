package com.goodee.beedan.controller.stock;

import com.goodee.beedan.dto.stock.StockListDto;
import com.goodee.beedan.service.stock.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/stock/api")
@RequiredArgsConstructor
public class StockApiController {

    private final StockService stockService;

    @GetMapping("/list")
    public Page<StockListDto> list(
            @RequestParam(required = false) List<Long> brands,
            @RequestParam(required = false) List<String> categories,
            @RequestParam(defaultValue = "popularity") String sort,
            @RequestParam(defaultValue = "0") int page) {
        Long memId = 1L;
        return stockService.findFiltered(brands, categories, sort, page, memId);
    }
}
