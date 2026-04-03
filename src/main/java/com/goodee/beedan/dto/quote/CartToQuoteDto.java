package com.goodee.beedan.dto.quote;

import com.goodee.beedan.entity.HsCode;
import com.goodee.beedan.entity.QuoteDetail;
import com.goodee.beedan.entity.Stock;
import com.goodee.beedan.entity.UnitGroup;
import lombok.Builder;
import lombok.Getter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class CartToQuoteDto {

    private List<Item> items;

    @Getter
    @Builder
    public static class Item {
        private int no;
        private Long stId;
        private String stNm;
        private String stBrNm;
        private String stCd;
        private Long qty;
        private String unGNm;
        private int unGQn;
        private int dozenCount;
        private BigDecimal stPr;
        private BigDecimal totalPr;
        private String stCur;
        private String hsCd;
        private String hsNm;
        private BigDecimal hsDuRa;
        // 임시저장 복원용
        private BigDecimal savedSubtotalKrw;
        private Long savedRcId;
        private List<ShipCard> savedShipCards;

        @Getter
        @Builder
        public static class ShipCard {
            private int qty;
            private String region;
            private String name;
            private String addr;
            private String phone;
            private String memo;
        }

        private static final ObjectMapper MAPPER = new ObjectMapper();

        public String getSavedShipCardsJson() {
            if (savedShipCards == null || savedShipCards.isEmpty()) return "";
            try { return MAPPER.writeValueAsString(savedShipCards); }
            catch (JsonProcessingException e) { return ""; }
        }

        public static Item of(int no, Stock stock, Long qty, UnitGroup unitGroup) {
            return of(no, stock, qty, unitGroup, null);
        }

        public static Item of(int no, Stock stock, Long qty, UnitGroup unitGroup, HsCode hsCode) {
            int dozen = (unitGroup != null && unitGroup.getUnGQn() > 0)
                    ? (int) Math.ceil((double) qty / unitGroup.getUnGQn())
                    : qty.intValue();

            return Item.builder()
                    .no(no)
                    .stId(stock.getStId())
                    .stNm(stock.getStNm())
                    .stBrNm(stock.getStBrNm())
                    .stCd(stock.getStCd())
                    .qty(qty)
                    .unGNm(unitGroup != null ? unitGroup.getUnGNm() : "-")
                    .unGQn(unitGroup != null ? unitGroup.getUnGQn() : 1)
                    .dozenCount(dozen)
                    .stPr(stock.getStPr())
                    .totalPr(stock.getStPr().multiply(BigDecimal.valueOf(qty)))
                    .stCur(stock.getStCur())
                    .hsCd(hsCode != null ? hsCode.getHsCd() : null)
                    .hsNm(hsCode != null ? hsCode.getHsNm() : null)
                    .hsDuRa(hsCode != null ? hsCode.getHsDuRa() : null)
                    .build();
        }

        /** 임시저장된 QuoteDetail에서 복원 (단건 — 하위 호환용) */
        public static Item fromDraft(int no, Stock stock, QuoteDetail detail, UnitGroup unitGroup, HsCode hsCode) {
            Long qty = detail.getQuDtQn() != null ? detail.getQuDtQn().longValue() : 1L;
            int uqn = detail.getQuUQn() != null ? detail.getQuUQn() : (unitGroup != null ? unitGroup.getUnGQn() : 1);
            int dozen = uqn > 0 ? (int) Math.ceil((double) qty / uqn) : qty.intValue();

            return Item.builder()
                    .no(no)
                    .stId(stock.getStId())
                    .stNm(stock.getStNm())
                    .stBrNm(stock.getStBrNm())
                    .stCd(stock.getStCd())
                    .qty(qty)
                    .unGNm(detail.getUnGNm() != null ? detail.getUnGNm() : (unitGroup != null ? unitGroup.getUnGNm() : "-"))
                    .unGQn(uqn)
                    .dozenCount(dozen)
                    .stPr(stock.getStPr())
                    .totalPr(stock.getStPr().multiply(BigDecimal.valueOf(qty)))
                    .stCur(stock.getStCur())
                    .hsCd(hsCode != null ? hsCode.getHsCd() : null)
                    .hsNm(hsCode != null ? hsCode.getHsNm() : null)
                    .hsDuRa(hsCode != null ? hsCode.getHsDuRa() : null)
                    .savedSubtotalKrw(detail.getQuDtPr())
                    .savedRcId(detail.getRcId())
                    .build();
        }

        /** 분할배송 그룹에서 복원 (여러 QuoteDetail → 1 Item) */
        public static Item fromDraftGroup(int no, Stock stock, java.util.List<QuoteDetail> group,
                                          UnitGroup unitGroup, HsCode hsCode) {
            QuoteDetail first = group.get(0);
            // 수량·소계 합산
            long totalQty = group.stream()
                    .mapToLong(d -> d.getQuDtQn() != null ? d.getQuDtQn() : 0).sum();
            BigDecimal totalSubtotal = group.stream()
                    .map(d -> d.getQuDtPr() != null ? d.getQuDtPr() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            int uqn = first.getQuUQn() != null ? first.getQuUQn() : (unitGroup != null ? unitGroup.getUnGQn() : 1);
            int dozen = uqn > 0 ? (int) Math.ceil((double) totalQty / uqn) : (int) totalQty;

            // shipCards 재구성
            java.util.List<ShipCard> shipCards = group.stream()
                    .map(d -> ShipCard.builder()
                            .qty(d.getQuDtQn() != null ? d.getQuDtQn() : 0)
                            .region(d.getQuDtRcRgn())
                            .name(d.getQuDtRcNm())
                            .addr(d.getQuDtRcAdr())
                            .phone(d.getQuDtRcPhn())
                            .memo(d.getQuDtRcMemo())
                            .build())
                    .collect(java.util.stream.Collectors.toList());

            return Item.builder()
                    .no(no)
                    .stId(stock.getStId())
                    .stNm(stock.getStNm())
                    .stBrNm(stock.getStBrNm())
                    .stCd(stock.getStCd())
                    .qty(totalQty)
                    .unGNm(first.getUnGNm() != null ? first.getUnGNm() : (unitGroup != null ? unitGroup.getUnGNm() : "-"))
                    .unGQn(uqn)
                    .dozenCount(dozen)
                    .stPr(stock.getStPr())
                    .totalPr(stock.getStPr().multiply(BigDecimal.valueOf(totalQty)))
                    .stCur(stock.getStCur())
                    .hsCd(hsCode != null ? hsCode.getHsCd() : null)
                    .hsNm(hsCode != null ? hsCode.getHsNm() : null)
                    .hsDuRa(hsCode != null ? hsCode.getHsDuRa() : null)
                    .savedSubtotalKrw(totalSubtotal)
                    .savedRcId(first.getRcId())
                    .savedShipCards(shipCards.size() > 1 ? shipCards : null)
                    .build();
        }
    }
}
