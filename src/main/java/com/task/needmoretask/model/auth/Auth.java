package com.task.needmoretask.model.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "auth_tb")
public class Auth {
    @Id @GeneratedValue
    private Long id;
    private Long userId;
    private String accessToken;
//    private String refreshToken;
    private boolean isRevoked;

    public void isRevokedAccessToken(String accessToken){
        this.accessToken = accessToken;
        this.isRevoked = true;
    }
}
