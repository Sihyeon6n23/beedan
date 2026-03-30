package com.goodee.beedan.controller.payment;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/payment")
public class PaymentController {

    @GetMapping("/check")
    public String getCheck(Model model) {
        model.addAttribute("activeStep", 4);
        return "/payment/payment-check";
    }

    @GetMapping("/receipt")
    public String getReceipt() {
        return "/payment/payment-receipt";
    }

    @GetMapping("/quote-detail")
    public String getQuoteDetail(Model model) {
        model.addAttribute("activeStep", 5);
        return "/payment/payment-quote-detail";
    }


    @GetMapping("/fail")
    public String getFail() {
        return "/payment/payment-fail";
    }

}
