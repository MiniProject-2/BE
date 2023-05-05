package com.task.needmoretask.controller;

import com.task.needmoretask.dto.ResponseDTO;
import com.task.needmoretask.dto.user.UserResponse;
import com.task.needmoretask.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RequestMapping("/api")
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    //프로필 업로드
    @PostMapping(value = "/user/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateImage(@RequestParam(value = "profileImage") MultipartFile image) throws IOException {
        UserResponse.ProfileOut url = userService.updateImage(image);
        return ResponseEntity.ok().body(new ResponseDTO<>(url));
    }
}
