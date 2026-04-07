package com.goodee.beedan.controller.quote;

import com.goodee.beedan.service.pdf.QuotePdfService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class QuotePdfController {

    private final QuotePdfService quotePdfService;

    @GetMapping("/api/quote/{quId}/pdf")
    public ResponseEntity<byte[]> downloadQuotePdf(@PathVariable Long quId) {
        try {
            byte[] pdf = quotePdfService.generateQuotePdf(quId);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=quote-" + quId + ".pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/api/payment/{quId}/tax-invoice")
    public ResponseEntity<byte[]> downloadTaxInvoice(@PathVariable Long quId) {
        try {
            byte[] pdf = quotePdfService.generateTaxInvoice(quId);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=tax-invoice-" + quId + ".pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/api/payment/{quId}/receipt")
    public ResponseEntity<byte[]> downloadReceipt(@PathVariable Long quId) {
        try {
            byte[] pdf = quotePdfService.generateReceipt(quId);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=receipt-" + quId + ".pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
