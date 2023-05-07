package com.task.needmoretask.dto.user;

import com.task.needmoretask.model.profile.Profile;
import com.task.needmoretask.model.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Getter
public class UserRequest {

    @Getter
    @Builder
    @AllArgsConstructor
    public static class JoinIn {
        
        @NotBlank
        private String email;
        @NotBlank
        @Pattern(regexp = "^[a-zA-Z.-]{6,16}$")
        private String password;
        @NotBlank
        private String passwordCheck;
        @NotBlank
        private String phone;
        @NotBlank
        private String fullName;
        @NotBlank
        private User.Department department;
        @NotBlank
        private Integer joinCompanyYear;
        @NotBlank
        private Long profileId;

    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class UserIn {
        private String password;
        private String passwordCheck;
        @NotBlank
        @Pattern(regexp = "^010-[0-9]{4}-[0-9]{4}$")
        private String phone;
        @NotBlank
        private String fullName;
        @NotNull
        private User.Department department;
        @NotNull
        private Integer joinCompanyYear;
        @NotNull
        private Long profileId;
    }
}
