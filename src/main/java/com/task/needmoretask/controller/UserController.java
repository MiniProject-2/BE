package com.task.needmoretask.controller;

import com.task.needmoretask.core.auth.session.MyUserDetails;
import com.task.needmoretask.dto.ResponseDTO;
import com.task.needmoretask.dto.user.UserRequest;
import com.task.needmoretask.dto.user.UserResponse;
import com.task.needmoretask.model.user.User;
import com.task.needmoretask.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;

@RequestMapping("/api")
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    //회원가입
    @PostMapping("/join")
    public ResponseEntity<?> join(@RequestBody @Valid UserRequest.JoinIn joinIn, Errors errors) {
        userService.join(joinIn);
        return ResponseEntity.ok().body(new ResponseDTO<>());
    }

    //프로필 업로드
    @PostMapping(value = "/user/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateImage(@RequestParam(value = "profileImage") MultipartFile image) throws IOException {
        UserResponse.ProfileOut url = userService.updateImage(image);
        return ResponseEntity.ok().body(new ResponseDTO<>(url));
    }

    //유져 조회
    @GetMapping("/admin/users")
    public ResponseEntity<?> getUsers(@RequestParam("page") int page) {
        Pageable pageable = PageRequest.of(page, 10);
        UserResponse.UsersOut users = userService.getUsers(pageable);
        return ResponseEntity.ok().body(new ResponseDTO<>(users));
    }

    //유저 검색
    @GetMapping("/users/search")
    public ResponseEntity<?> searchUsers(@RequestParam("fullName") String fullName, @RequestParam("page") int page) {
        Pageable pageable = PageRequest.of(page, 10);
        UserResponse.UsersOut users = userService.searchUsers(fullName, pageable);
        return ResponseEntity.ok().body(new ResponseDTO<>(users));
    }

    //개인정보 조회
    @GetMapping("/user/{id}")
    public ResponseEntity<?> getUserInfo(@PathVariable Long id, @AuthenticationPrincipal MyUserDetails myUserDetails) {
        UserResponse.UserOut user = userService.getUserInfo(id, myUserDetails.getUser());
        return ResponseEntity.ok().body(new ResponseDTO<>(user));
    }

    //개인정보 수정
    @PutMapping("/user/{id}")
    public ResponseEntity<?> updateUserInfo(@PathVariable Long id, @RequestBody @Valid UserRequest.UserIn userIn, @AuthenticationPrincipal MyUserDetails myUserDetails) {
        UserResponse.UserOut user = userService.updateUserInfo(id, userIn, myUserDetails.getUser());
        return ResponseEntity.ok().body(new ResponseDTO<>(user));
    }
}
