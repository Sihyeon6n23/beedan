package com.goodee.beedan.controller.stock;

import com.goodee.beedan.dto.stock.StockListDto;
import com.goodee.beedan.entity.Brand;
import com.goodee.beedan.entity.Category;
import com.goodee.beedan.service.stock.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/stock")
public class StockController {
    private final StockService stockService;

    // 상품 목록
    @GetMapping("/list")
    public String getStocks(
            @PageableDefault(page = 0, size = 8, sort = "stCreDt", direction = Sort.Direction.DESC)
            Pageable pageable,
            Model model) {
        Page<StockListDto> page = stockService.findAllStocks(pageable);
        List<Brand> brands = stockService.findAllBrands();
        List<Category> categories = stockService.findAllCategories();

        model.addAttribute("page", page);
        model.addAttribute("brands", brands);
        model.addAttribute("categories", categories);
        return "stock/stock-list";
    }
}
