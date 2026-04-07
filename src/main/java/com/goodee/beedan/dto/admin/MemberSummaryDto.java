package com.goodee.beedan.dto.admin;

import com.goodee.beedan.common.constant.InquiryStatus;
import com.goodee.beedan.common.constant.OrderStatus;
import com.goodee.beedan.common.constant.ShipmentStatus;
import com.goodee.beedan.dto.board.InquiryBoardListDto;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Builder
@AllArgsConstructor
public class MemberSummaryDto {
    private String memLgnId;
    private LocalDateTime memCreDt;
    private String memNm;

    private List<OrderSummaryDto> recentOrders;
    private List<ShipmentSummaryDto> recentShipments;
    private Page<InquiryBoardListDto> recentInquiries;

    @Getter @Builder
    public static class OrderSummaryDto {
        private Long ordBaseId;
        private String ordBaseNo;
        private String ordSummaryNm;
        private BigDecimal ordBaseTtAm;
        private LocalDateTime ordBaseCreDt;
        @Enumerated(EnumType.STRING)
        private OrderStatus ordBaseStt;
    }

    @Getter @Builder
    public static class ShipmentSummaryDto {  // 운송장 번호, 출발지, 도착지 주소, 수령자 이름
        private Long shId;
        private String shTraNo;
        private String shCarCd;
        private ShipmentStatus shStt;
        private String shRcvNm;
        private String shAdr;
        private String shAdrDt;
    }


    @Getter @Builder
    public static class InquirySummaryDto {
        private Long brdId;                 // 글 id
        private String brdTtl;              // 글 제목
        private InquiryStatus brdInqStt;    // 문의 상태
        private LocalDateTime brdCreDt;     // 글 작성 일시
        private Boolean hasReply;           // 답글 존재 여부
        private Boolean replyEdited;        // 답글 수정 여부
    }
}
