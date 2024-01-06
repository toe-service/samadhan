package com.samadhan.service;

import com.samadhan.payloads.Payloads;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class TestGaadiService {

    @Autowired
    private GaadiService gaadiService;

    @Test
    void testLoginUserNotFound() throws Exception {
       Assertions.assertEquals("user not found", gaadiService.login(Payloads.getLoginRequest()));
    }
}
