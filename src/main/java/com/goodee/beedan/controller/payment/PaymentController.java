package com.goodee.beedan.controller.payment;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/payment")
public class PaymentController {

    @GetMapping("/check")
    public String getCheck() {
        return "/payment/payment-check";
    }

    @GetMapping("/receipt")
    public String getReceipt() {
        return "/payment/payment-receipt";
    }






    @GetMapping("/success")
    public String getSuccess() {
        return "/payment/payment-success";
    }
    @GetMapping("/fail")
    public String getFail() {
        return "/payment/payment-fail";
    }

}
