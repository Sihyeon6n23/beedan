package com.goodee.beedan.devUtils;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

@Component
public class AppDateTime {

    private static Duration offset = Duration.ZERO;

    public static LocalDateTime now() {
        return LocalDateTime.now().plus(offset);
    }

    public static void setNow(LocalDateTime targetNow) {
        offset = Duration.between(LocalDateTime.now(), targetNow);
    }

    public static void reset() {
        offset = Duration.ZERO;
    }


}
