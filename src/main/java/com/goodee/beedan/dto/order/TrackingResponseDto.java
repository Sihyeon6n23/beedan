package com.goodee.beedan.dto.order;

import com.goodee.beedan.dto.admin.MemberSummaryDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
public class TrackingResponseDto {
    private String carrierName;
    private String trackingNumber;
    private String statusText;
    private String shAdr;
    private String shRcvNm;

    private List<TrackingDetailDto> details; // 국내 택배
    private List<TrackingDetail> customsDetails; // 통관

    @Data @Builder
    @NoArgsConstructor @AllArgsConstructor
    public static class TrackingDetail {
        private String time;
        private String status;
        private String description;
    }
}