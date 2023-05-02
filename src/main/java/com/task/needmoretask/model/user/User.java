package com.task.needmoretask.model.user;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Profile;

import javax.persistence.*;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "user_tb")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String email;
    private String password;
    private String phone;
    private String fullname;
    private Department department;
    private Integer joinCompanyYear;
    private Profile profile;
    private Role role;
    private boolean status;

    public enum Department {
        DEVELOPMENT,
        HR,
        MANAGEMENT,
        TRADE,
        SALES,
        SERVICE,
        PRODUCTION,
        EDUCATION,
        MARKETING,
        OTHERS
    }

    public enum Role {
        USER,
        ADMIN,
    }

    @Builder
    public User(Long id, String email, String password, String phone, String fullname, Department department, Integer joinCompanyYear, Profile profile, Role role, boolean status) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.fullname = fullname;
        this.department = department;
        this.joinCompanyYear = joinCompanyYear;
        this.profile = profile;
        this.role = role;
        this.status = status;
    }

    void update(User user) {
        // TODO
    }

    void updateRole(Role role) {
        this.role = role;
    }

    void updateProfile(Profile profile) {
        // TODO
    }

    void deactivateAccount(boolean status) {
        this.status = false;
    }

    void checkEmailDuplicate(String email) {
        // TODO
    }
}
