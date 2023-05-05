package com.task.needmoretask.service;

import com.task.needmoretask.core.exception.Exception400;
import com.task.needmoretask.core.util.S3Uploader;
import com.task.needmoretask.dto.user.UserResponse;
import com.task.needmoretask.model.profile.Profile;
import com.task.needmoretask.model.profile.ProfileRepository;
import com.task.needmoretask.model.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

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
}
