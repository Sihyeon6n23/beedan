package com.goodee.beedan.unipassTests;

import com.goodee.beedan.service.order.UnipassService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class UnipassServiceTest {

    @Autowired
    private UnipassService unipassService;

    @Test
    void UnipassTest() {
        String testHblNo = "";
        String testYear = "";

        String response = unipassService.getCargoStatus(testHblNo, testYear);

        System.out.println("========================================");
        System.out.println("응답 결과 확인:");
        System.out.println(response);
        System.out.println("========================================");
    }
}