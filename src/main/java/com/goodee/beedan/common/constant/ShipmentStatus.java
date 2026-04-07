package com.goodee.beedan.common.constant;

public enum ShipmentStatus {
    DELIVERED,
    DELIVERING,
    SHIPPING,
    PREPARING;

    public String getStatusName() {
        return switch (this) {
            case PREPARING -> "상품준비중";
            case SHIPPING -> "통관처리";
            case DELIVERING -> "배송중";
            case DELIVERED -> "배송완료";
        };
    }

}
