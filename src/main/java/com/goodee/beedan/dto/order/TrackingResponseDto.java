package com.goodee.beedan.dto.order;

import com.goodee.beedan.dto.admin.MemberSummaryDto;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TrackingResponseDto {
    private String carrierName;
    private String trackingNumber;
    private String statusText;
    private List<TrackingDetailDto> details;
}