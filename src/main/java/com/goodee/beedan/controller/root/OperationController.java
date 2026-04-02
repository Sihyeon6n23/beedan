package com.goodee.beedan.controller.root;

import com.goodee.beedan.entity.BuyerGradePolicy;
import com.goodee.beedan.entity.FeePolicy;
import com.goodee.beedan.repository.quote.ShippingInsuranceRepository;
import com.goodee.beedan.repository.quote.StockInspectionRepository;
import com.goodee.beedan.service.buyer.BuyerGradePolicyService;
import com.goodee.beedan.service.buyer.FeePolicyService;
import com.goodee.beedan.service.quote.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class OperationController {

    private final BuyerGradePolicyService buyerGradePolicyService;
    private final FeePolicyService feePolicyService;
    private final ShippingInsuranceRepository shippingInsuranceRepository;
    private final StockInspectionRepository stockInspectionRepository;
    private final UnitGroupService unitGroupService;
    private final UnitDiscountService unitDiscountService;
    private final ShippingRateService shippingRateService;
    private final PortCustomsRateService portCustomsRateService;
    private final DomesticDeliveryRateService domesticDeliveryRateService;

    @GetMapping("/root/operation")
    public String getOperationPolicy(Model model) {

        model.addAttribute("grades", buyerGradePolicyService.findAllActive());
        model.addAttribute("insurances", shippingInsuranceRepository.findAllBySiYnTrue());
        model.addAttribute("inspections", stockInspectionRepository.findAllByStiYnTrue());
        model.addAttribute("unitGroups", unitGroupService.findAllActive());
        model.addAttribute("shippingRates", shippingRateService.findAllActive());
        model.addAttribute("portCustomsRates", portCustomsRateService.findAllActive());
        model.addAttribute("deliveryRates", domesticDeliveryRateService.findAllActive());

        return "root/operation/operation-policy";
    }

    @GetMapping("/root/operation/fee-policy/{bgpId}")
    public String getFeePolicyDetail(@PathVariable Long bgpId, Model model) {

        BuyerGradePolicy grade = buyerGradePolicyService.findById(bgpId);
        List<FeePolicy> feePolicies = feePolicyService.findAllActiveByGrade(grade.getBgpGr());

        model.addAttribute("grade", grade);
        model.addAttribute("feePolicies", feePolicies);

        return "root/operation/fee-policy";
    }
}
