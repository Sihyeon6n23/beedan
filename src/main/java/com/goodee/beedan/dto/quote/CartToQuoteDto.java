package com.goodee.beedan.dto.quote;

import com.goodee.beedan.entity.HsCode;
import com.goodee.beedan.entity.Stock;
import com.goodee.beedan.entity.UnitGroup;
import lombok.Builder;
import lombok.Getter;

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
    }
}
