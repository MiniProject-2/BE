package com.task.needmoretask.controller;

import com.task.needmoretask.dto.ResponseDTO;
import com.task.needmoretask.dto.user.UserResponse;
import com.task.needmoretask.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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

    //유져 조회
    @GetMapping("/admin/users")
    public ResponseEntity<?> getUsers(@RequestParam("page") int page){
        Pageable pageable = PageRequest.of(page,10);
        UserResponse.UsersOut users = userService.getUsers(pageable);
        return ResponseEntity.ok().body(new ResponseDTO<>(users));
    }
}
