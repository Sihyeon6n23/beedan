package com.goodee.beedan.service.pdf;

import com.goodee.beedan.entity.*;
import com.goodee.beedan.repository.buyer.BuyerGradePolicyRepository;
import com.goodee.beedan.repository.member.MemberRepository;
import com.goodee.beedan.repository.payment.PaymentRepository;
import com.goodee.beedan.repository.quote.QuoteInfoRepository;
import com.goodee.beedan.repository.quote.ShippingInsuranceRepository;
import com.goodee.beedan.repository.quote.StockInspectionRepository;
import com.goodee.beedan.repository.stock.StockRepository;
import com.goodee.beedan.service.quote.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
public class QuotePdfService {

    private final QuoteBaseService quoteBaseService;
    private final QuoteDetailService quoteDetailService;
    private final QuoteInfoRepository quoteInfoRepository;
    private final QuoteShipFeeService quoteShipFeeService;
    private final NegotiationService negotiationService;
    private final MemberRepository memberRepository;
    private final StockRepository stockRepository;
    private final BuyerGradePolicyRepository buyerGradePolicyRepository;
    private final ShippingInsuranceRepository shippingInsuranceRepository;
    private final StockInspectionRepository stockInspectionRepository;
    private final PaymentRepository paymentRepository;
    private final TemplateEngine templateEngine;

    public byte[] generateQuotePdf(Long quId) throws Exception {
        QuoteBase quoteBase = quoteBaseService.findById(quId);
        QuoteInfo quoteInfo = quoteInfoRepository.findByQuId(quId).orElse(null);
        List<QuoteDetail> details = quoteDetailService.findAllByQuote(quId);
        List<QuoteShipFee> shipFees = quoteShipFeeService.findAllByQuote(quId);
        Negotiation negotiation = negotiationService.findById(quoteBase.getNgId());

        // 고객 정보
        Member member = null;
        if (negotiation.getMemId() != null) {
            member = memberRepository.findById(negotiation.getMemId()).orElse(null);
        }

        // 품목별 부가 정보
        List<Map<String, Object>> detailExtras = new ArrayList<>();
        BigDecimal itemTotalKrw = BigDecimal.ZERO;
        for (QuoteDetail d : details) {
            Map<String, Object> extra = new LinkedHashMap<>();
            Stock stock = d.getStId() != null ? stockRepository.findById(d.getStId()).orElse(null) : null;
            extra.put("stCd", stock != null ? stock.getStCd() : "-");
            extra.put("stCur", stock != null ? stock.getStCur() : "");
            if (d.getQuUQn() != null && d.getQuUQn() > 0 && d.getQuDtQn() != null) {
                extra.put("spec", (int) Math.ceil((double) d.getQuDtQn() / d.getQuUQn()));
            } else {
                extra.put("spec", null);
            }
            detailExtras.add(extra);
            if (d.getQuDtPr() != null) itemTotalKrw = itemTotalKrw.add(d.getQuDtPr());
        }

        // 운임/관세 계산 (detail과 동일)
        BigDecimal intShipFeeOnly = BigDecimal.ZERO;
        BigDecimal totalDutyVat = BigDecimal.ZERO;
        for (QuoteShipFee sf : shipFees) {
            BigDecimal sfShip = sf.getQsfSrAm() != null ? sf.getQsfSrAm() : BigDecimal.ZERO;
            BigDecimal sfPort = sf.getQsfPrtAm() != null ? sf.getQsfPrtAm() : BigDecimal.ZERO;
            BigDecimal sfCust = sf.getQsfCstAm() != null ? sf.getQsfCstAm() : BigDecimal.ZERO;
            BigDecimal sfHs = sf.getQsfHsCd() != null ? sf.getQsfHsCd() : BigDecimal.ZERO;
            BigDecimal sfIns = (sf.getQsfInsYn() != null && sf.getQsfInsYn() && sf.getQsfInsAm() != null)
                    ? sf.getQsfInsAm() : BigDecimal.ZERO;
            intShipFeeOnly = intShipFeeOnly.add(sfShip).add(sfPort).add(sfCust).add(sfHs).add(sfIns);
            BigDecimal sfDuty = sf.getQsfDty() != null ? sf.getQsfDty() : BigDecimal.ZERO;
            BigDecimal sfVat = sf.getQsfVat() != null ? sf.getQsfVat() : BigDecimal.ZERO;
            totalDutyVat = totalDutyVat.add(sfDuty).add(sfVat);
        }
        BigDecimal domesticFee = quoteInfo != null && quoteInfo.getQuInfoDomShiFe() != null
                ? quoteInfo.getQuInfoDomShiFe() : BigDecimal.ZERO;
        BigDecimal serviceFeeAm = quoteInfo != null && quoteInfo.getQuInfoSrvFeAm() != null
                ? quoteInfo.getQuInfoSrvFeAm() : BigDecimal.ZERO;
        BigDecimal calculatedTotal = itemTotalKrw.add(intShipFeeOnly).add(domesticFee)
                .add(serviceFeeAm).add(totalDutyVat);

        // 등급
        String buyerGrade = "STANDARD";
        if (quoteInfo != null && quoteInfo.getBgpId() != null) {
            buyerGrade = buyerGradePolicyRepository.findById(quoteInfo.getBgpId())
                    .map(BuyerGradePolicy::getBgpGr).orElse("STANDARD");
        }

        // 보험/검사명
        String insuranceName = null;
        String inspectionName = null;
        if (quoteInfo != null) {
            if (quoteInfo.getSiId() != null) {
                insuranceName = shippingInsuranceRepository.findById(quoteInfo.getSiId())
                        .map(ShippingInsurance::getSiNm).orElse(null);
            }
            if (quoteInfo.getStiId() != null) {
                inspectionName = stockInspectionRepository.findById(quoteInfo.getStiId())
                        .map(StockInspection::getStiNm).orElse(null);
            }
        }

        // Thymeleaf 컨텍스트
        Context ctx = new Context();
        ctx.setVariable("quoteBase", quoteBase);
        ctx.setVariable("quoteInfo", quoteInfo);
        ctx.setVariable("details", details);
        ctx.setVariable("detailExtras", detailExtras);
        ctx.setVariable("shipFees", shipFees);
        ctx.setVariable("negotiation", negotiation);
        ctx.setVariable("member", member);
        ctx.setVariable("itemTotalKrw", itemTotalKrw);
        ctx.setVariable("intShipFeeOnly", intShipFeeOnly);
        ctx.setVariable("totalDutyVat", totalDutyVat);
        ctx.setVariable("calculatedTotal", calculatedTotal);
        ctx.setVariable("buyerGrade", buyerGrade);
        ctx.setVariable("insuranceName", insuranceName);
        ctx.setVariable("inspectionName", inspectionName);

        // HTML 렌더링
        String html = templateEngine.process("pdf/quote-pdf", ctx);

        // PDF 변환
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ITextRenderer renderer = new ITextRenderer();

        // 한글 폰트 등록
        String fontPath = getClass().getClassLoader().getResource("fonts/NotoSansKR-Regular.ttf").toExternalForm();
        renderer.getFontResolver().addFont(fontPath, "NotoSansKR",
                com.lowagie.text.pdf.BaseFont.IDENTITY_H, true, null);

        renderer.setDocumentFromString(html);
        renderer.layout();
        renderer.createPDF(baos);
        return baos.toByteArray();
    }

    /** 세금계산서 PDF */
    public byte[] generateTaxInvoice(Long quId) throws Exception {
        Context ctx = buildPaymentContext(quId);
        String html = templateEngine.process("pdf/tax-invoice-pdf", ctx);
        return renderPdf(html);
    }

    /** 결제 영수증 PDF */
    public byte[] generateReceipt(Long quId) throws Exception {
        Context ctx = buildPaymentContext(quId);
        String html = templateEngine.process("pdf/receipt-pdf", ctx);
        return renderPdf(html);
    }

    private Context buildPaymentContext(Long quId) {
        QuoteBase quoteBase = quoteBaseService.findById(quId);
        QuoteInfo quoteInfo = quoteInfoRepository.findByQuId(quId).orElse(null);
        List<QuoteDetail> details = quoteDetailService.findAllByQuote(quId);
        List<QuoteShipFee> shipFees = quoteShipFeeService.findAllByQuote(quId);
        Negotiation negotiation = negotiationService.findById(quoteBase.getNgId());
        Payment payment = paymentRepository.findFirstByQuIdOrderByPyIdDesc(quId).orElse(null);
        Member member = negotiation.getMemId() != null
                ? memberRepository.findById(negotiation.getMemId()).orElse(null) : null;

        // 금액 계산
        BigDecimal itemTotalKrw = BigDecimal.ZERO;
        for (QuoteDetail d : details) {
            if (d.getQuDtPr() != null) itemTotalKrw = itemTotalKrw.add(d.getQuDtPr());
        }
        BigDecimal intShipFeeOnly = BigDecimal.ZERO;
        BigDecimal totalDutyVat = BigDecimal.ZERO;
        for (QuoteShipFee sf : shipFees) {
            intShipFeeOnly = intShipFeeOnly
                    .add(sf.getQsfSrAm() != null ? sf.getQsfSrAm() : BigDecimal.ZERO)
                    .add(sf.getQsfPrtAm() != null ? sf.getQsfPrtAm() : BigDecimal.ZERO)
                    .add(sf.getQsfCstAm() != null ? sf.getQsfCstAm() : BigDecimal.ZERO)
                    .add(sf.getQsfHsCd() != null ? sf.getQsfHsCd() : BigDecimal.ZERO)
                    .add(sf.getQsfInsYn() != null && sf.getQsfInsYn() && sf.getQsfInsAm() != null
                            ? sf.getQsfInsAm() : BigDecimal.ZERO);
            totalDutyVat = totalDutyVat
                    .add(sf.getQsfDty() != null ? sf.getQsfDty() : BigDecimal.ZERO)
                    .add(sf.getQsfVat() != null ? sf.getQsfVat() : BigDecimal.ZERO);
        }
        BigDecimal domesticFee = quoteInfo != null && quoteInfo.getQuInfoDomShiFe() != null
                ? quoteInfo.getQuInfoDomShiFe() : BigDecimal.ZERO;
        BigDecimal serviceFeeAm = quoteInfo != null && quoteInfo.getQuInfoSrvFeAm() != null
                ? quoteInfo.getQuInfoSrvFeAm() : BigDecimal.ZERO;
        BigDecimal calculatedTotal = itemTotalKrw.add(intShipFeeOnly).add(domesticFee)
                .add(serviceFeeAm).add(totalDutyVat);

        Context ctx = new Context();
        ctx.setVariable("quoteBase", quoteBase);
        ctx.setVariable("quoteInfo", quoteInfo);
        ctx.setVariable("details", details);
        ctx.setVariable("negotiation", negotiation);
        ctx.setVariable("payment", payment);
        ctx.setVariable("member", member);
        ctx.setVariable("itemTotalKrw", itemTotalKrw);
        ctx.setVariable("intShipFeeOnly", intShipFeeOnly);
        ctx.setVariable("totalDutyVat", totalDutyVat);
        ctx.setVariable("calculatedTotal", calculatedTotal);
        ctx.setVariable("serviceFeeAm", serviceFeeAm);
        ctx.setVariable("domesticFee", domesticFee);
        return ctx;
    }

    private byte[] renderPdf(String html) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ITextRenderer renderer = new ITextRenderer();
        String fontPath = getClass().getClassLoader().getResource("fonts/NotoSansKR-Regular.ttf").toExternalForm();
        renderer.getFontResolver().addFont(fontPath, "NotoSansKR",
                com.lowagie.text.pdf.BaseFont.IDENTITY_H, true, null);
        renderer.setDocumentFromString(html);
        renderer.layout();
        renderer.createPDF(baos);
        return baos.toByteArray();
    }
}
