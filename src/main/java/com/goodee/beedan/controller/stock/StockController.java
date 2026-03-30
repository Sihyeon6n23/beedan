package com.goodee.beedan.controller.stock;

import com.goodee.beedan.config.security.MemberUserDetails;
import com.goodee.beedan.dto.stock.StockListDto;
import com.goodee.beedan.entity.Brand;
import com.goodee.beedan.entity.Category;
import com.goodee.beedan.service.stock.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.goodee.beedan.entity.Stock;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/stock")
public class StockController {
    private final StockService stockService;

    // 상품 목록
    @GetMapping("/list")
    public String getStocks(
            @PageableDefault(page = 0, size = 8, sort = "stId", direction = Sort.Direction.DESC)
            Pageable pageable,
            Model model,
            @AuthenticationPrincipal MemberUserDetails userDetails)
            {
                Long memId;
                if(userDetails == null) {
                    memId = null;
                } else {
                    memId = userDetails.getMemberId();
                }
                model.addAttribute("memberId", memId);
        Page<StockListDto> page = stockService.findAllStocks(pageable, memId);
        List<Brand> brands = stockService.findAllBrands();
        List<Category> categories = stockService.findAllCategories();

        model.addAttribute("page", page);
        model.addAttribute("brands", brands);
        model.addAttribute("categories", categories);
        return "stock/stock-list";
    }

    // 상품 상세
    @GetMapping("/detail/{stId}")
    public String getStockDetail(@PathVariable Long stId, Model model,
                                 @AuthenticationPrincipal MemberUserDetails userDetails) {
        Long memId = userDetails != null ? userDetails.getMemberId() : null;
        Stock stock = stockService.findById(stId);
        boolean wished = stockService.isWished(stId, memId);

        model.addAttribute("stock", stock);
        model.addAttribute("wished", wished);
        model.addAttribute("memberId", memId);
        return "stock/stock-detail";
    }
}
