package com.task.needmoretask.service;

import com.task.needmoretask.core.exception.Exception400;
import com.task.needmoretask.core.util.S3Uploader;
import com.task.needmoretask.dto.user.UserResponse;
import com.task.needmoretask.model.profile.Profile;
import com.task.needmoretask.model.profile.ProfileRepository;
import com.task.needmoretask.model.user.User;
import com.task.needmoretask.model.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final S3Uploader s3Uploader;

    //프로필 업로드
    @Transactional
    public UserResponse.ProfileOut updateImage(MultipartFile image) throws IOException {
        String dirName = "images";
        if(image.isEmpty()) throw new Exception400("profile","이미지가 전송되지 않았습니다");
        String storedFileName = s3Uploader.upload(image, dirName);
        Profile profile = Profile.builder().url(storedFileName).build();
        profileRepository.save(profile);
        return new UserResponse.ProfileOut(profile);
    }

    //유저 조회
    public UserResponse.UsersOut getUsers(Pageable pageable){
        Page<User> users = userRepository.findAll(pageable);
        List<UserResponse.UsersOut.UserOut> userOut = users.stream()
                .map(UserResponse.UsersOut.UserOut::new)
                .collect(Collectors.toList());
        return new UserResponse.UsersOut(userOut,users.isLast());
    }

    //유저 검색
    public UserResponse.UsersOut searchUsers(String fullName, Pageable pageable){
        Page<User> users = userRepository.findUsersByFullName(fullName,pageable);
        List<UserResponse.UsersOut.UserOut> userOut = users.stream()
                .map(UserResponse.UsersOut.UserOut::new)
                .collect(Collectors.toList());
        return new UserResponse.UsersOut(userOut,users.isLast());
    }
}
