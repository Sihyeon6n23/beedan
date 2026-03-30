package com.goodee.beedan.controller.stock;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/stock")
public class AdminStockController {

    @GetMapping("/list")
    public String StockList() {
        return "admin/stock/stock-list";
    }

}


