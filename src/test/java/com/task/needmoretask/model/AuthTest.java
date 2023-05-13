package com.task.needmoretask.model;

import com.task.needmoretask.model.log.Log;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthTest {

    @Test
    void isRevokedAccessToken() {
        //given
        String userAgent = "APPLE";
        String clientIp = "0.0.0";
        Log log = Log.builder()
                .userAgent("")
                .clientIP("")
                .build();
        //when
        log.LastLoginDate(userAgent,clientIp);
        //then
        Assertions.assertEquals(userAgent,log.getUserAgent());
        Assertions.assertEquals(clientIp,log.getClientIP());
    }
}