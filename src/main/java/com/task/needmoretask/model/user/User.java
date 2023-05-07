package com.task.needmoretask.model.user;

import com.task.needmoretask.dto.user.UserRequest;
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
    @Column(nullable = false, unique = true, length = 50)
    private String email;
    @Column(nullable = false, length = 60)
    private String password;
    @Column(nullable = false)
    private String phone;
    @Column(nullable = false)
    private String fullname;
    @Column(nullable = false)
    private Department department;
    @Column(nullable = false)
    private Integer joinCompanyYear;
    @ManyToOne(fetch = FetchType.LAZY)
    private Profile profile;
    @Column(nullable = false)
    private Role role;
    @Column(nullable = false)
    private boolean isDeleted;


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
    public User(Long id, String email, String password, String phone, String fullname, Department department, Integer joinCompanyYear, Profile profile, Role role) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.fullname = fullname;
        this.department = department;
        this.joinCompanyYear = joinCompanyYear;
        this.profile = profile;
        this.role = role;
        this.isDeleted = false;
    }

    public User(UserRequest.JoinIn joinIn, Profile profile, String password) {
        this.email = joinIn.getEmail();
        this.password = joinIn.getPassword();
        this.phone = joinIn.getPhone();
        this.fullname = joinIn.getFullName();
        this.department = joinIn.getDepartment();
        this.joinCompanyYear = joinIn.getJoinCompanyYear();
        this.profile = profile;
        this.role = Role.USER;
        this.isDeleted = false;
    }

    public void pwdUpdate(String password) {
        this.password = password;
    }

    public void update(UserRequest.UserIn userIn, Profile profile) {
        this.phone = userIn.getPhone();
        this.fullname = userIn.getFullName();
        this.department = userIn.getDepartment();
        this.joinCompanyYear = userIn.getJoinCompanyYear();
        this.profile = profile;
    }

    public void update(UserRequest.UserIn userIn) {
        this.phone = userIn.getPhone();
        this.fullname = userIn.getFullName();
        this.department = userIn.getDepartment();
        this.joinCompanyYear = userIn.getJoinCompanyYear();
    }

    public void updateRole(Role role) {
        this.role = role;
    }

    public void updateProfile(Profile profile) {
        // TODO
    }

    public void deactivateAccount() {
        this.isDeleted = true;
    }

    public void checkEmailDuplicate(String email) {
        // TODO
    }

}
