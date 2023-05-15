package com.task.needmoretask.model;

import com.task.needmoretask.model.auth.Auth;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class LogTest {

    @Test
    void lastLoginDate() {
        //given
        String accessToken = "accessToken";
        Auth auth = Auth.builder()
                .accessToken("")
                .build();
        //when
        auth.isRevokedAccessToken(accessToken);
        //then
        Assertions.assertEquals(accessToken,auth.getAccessToken());
    }
}