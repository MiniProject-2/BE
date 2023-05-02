package com.task.needmoretask.model.user;

import com.task.needmoretask.model.profile.Profile;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "user_tb")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String email;
    @Column(nullable = false)
    private String password;
    @Column(nullable = false)
    private String phone;
    @Column(nullable = false)
    private String fullname;
    @Column(nullable = false)
    private Department department;
    @Column(nullable = false)
    private Integer joinCompanyYear;
    @Column(nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Profile profile;
    @Column(nullable = false)
    private Role role;
    @Column(nullable = false)
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
