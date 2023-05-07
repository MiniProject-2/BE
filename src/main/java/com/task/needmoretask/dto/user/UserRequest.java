package com.task.needmoretask.dto.user;

import com.task.needmoretask.model.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Getter
public class UserRequest {

    @Getter @Builder
    @AllArgsConstructor
    public static class Login{
        @Email
        @NotBlank
        private String email;
        @NotBlank
        @Pattern(regexp = "^[a-zA-Z0-9.-]{6,16}$")
        private String password;
    }

    @Getter @Builder
    @AllArgsConstructor
    public static class UserIn{
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

    @Getter @Builder
    @AllArgsConstructor
    public static class updateRoleInDTO{
        @NotNull
        Long userId;
        @NotNull
        User.Role role;
    }

    @Getter
    @Builder
    public static class UserPasswordValidate{
        @NotBlank
        @Pattern(regexp = "^[a-zA-Z0-9.-]{6,16}$")
        private String password;
        @NotBlank
        @Pattern(regexp = "^[a-zA-Z0-9.-]{6,16}$")
        private String passwordCheck;

        public UserPasswordValidate(String password, String passwordCheck) {
            this.password = password;
            this.passwordCheck = passwordCheck;
        }
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserEmailValidate{
        @NotBlank
        @Email
        private String email;
    }
}