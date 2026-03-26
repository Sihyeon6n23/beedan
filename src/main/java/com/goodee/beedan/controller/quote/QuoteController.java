package com.goodee.beedan.controller.quote;

import com.goodee.beedan.dto.cart.CartForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/quote")
@RequiredArgsConstructor
public class QuoteController {

    @PostMapping("/request")
    public String postItems(@RequestParam List<Long> stIds,
                            @RequestParam List<Long> qns,
                            Model model) {
        List<CartForm> ItemLists = new ArrayList<>();
        for(int i = 0; i < qns.size(); i++) {
            ItemLists.add(CartForm.builder()
                    .stId(stIds.get(i))
                    .caStQn(qns.get(i))
                    .build());
        }
        return "quote/quote-write";
    };

    @GetMapping("/list")
    public String getList() {
        return "/quote/quote-list";
    }

    @GetMapping("/write")
    public String getWrite() {
        return "/quote/quote-write";
    }

    @GetMapping("/detail")
    public String getDetail() { return "/quote/quote-detail"; }

}
