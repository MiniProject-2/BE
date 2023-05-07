package com.task.needmoretask.service;

import com.task.needmoretask.core.auth.jwt.MyJwtProvider;
import com.task.needmoretask.core.exception.Exception400;
import com.task.needmoretask.core.exception.Exception401;
import com.task.needmoretask.core.exception.Exception403;
import com.task.needmoretask.core.exception.Exception404;
import com.task.needmoretask.core.util.S3Uploader;
import com.task.needmoretask.dto.user.UserRequest;
import com.task.needmoretask.dto.user.UserResponse;
import com.task.needmoretask.model.auth.Auth;
import com.task.needmoretask.model.auth.AuthRepository;
import com.task.needmoretask.model.log.Log;
import com.task.needmoretask.model.log.LogRepository;
import com.task.needmoretask.model.profile.Profile;
import com.task.needmoretask.model.profile.ProfileRepository;
import com.task.needmoretask.model.user.User;
import com.task.needmoretask.model.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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
    private final LogRepository logRepository;
    private final AuthRepository authRepository;
    private final S3Uploader s3Uploader;
    private final BCryptPasswordEncoder passwordEncoder;
    private final MyJwtProvider myJwtProvider;

    //로그인
    @Transactional
    public String login(UserRequest.Login login, String userAgent, String ipAddress){
        User user = userRepository.findUserByEmail(login.getEmail()).orElseThrow(
                () -> new Exception401("로그인에 실패했습니다")
        );
        validatePassword(login.getPassword(),user);

        // 로그에 유저 데이터가 있으면 update, 없으면 save
        logRepository.findLogByEmail(login.getEmail()).ifPresentOrElse(
                log -> log.LastLoginDate(userAgent,ipAddress),
                () -> logRepository.save(Log.builder()
                        .user(user)
                        .userAgent(userAgent)
                        .clientIP(ipAddress)
                        .build())
        );
        String accessToken = myJwtProvider.create(user);
        Auth auth = Auth.builder()
                .userId(user.getId())
                .accessToken(accessToken)
                .build();
        authRepository.save(auth);
        return accessToken;
    }

    //프로필 업로드
    @Transactional
    public UserResponse.ProfileOut updateImage(MultipartFile image) throws IOException {
        String dirName = "images";
        if (image.isEmpty()) throw new Exception400("profile", "이미지가 전송되지 않았습니다");
        String storedFileName = s3Uploader.upload(image, dirName);
        Profile profile = Profile.builder().url(storedFileName).build();
        profileRepository.save(profile);
        return new UserResponse.ProfileOut(profile);
    }

    //유저 조회
    public UserResponse.UsersOut getUsers(Pageable pageable) {
        Page<User> users = userRepository.findAll(pageable);
        List<UserResponse.UsersOut.UserOut> userOut = users.stream()
                .map(UserResponse.UsersOut.UserOut::new)
                .collect(Collectors.toList());
        return new UserResponse.UsersOut(userOut, users.isLast());
    }

    //유저 검색
    public UserResponse.UsersOut searchUsers(String fullName, Pageable pageable) {
        Page<User> users = userRepository.findUsersByFullName(fullName, pageable);
        List<UserResponse.UsersOut.UserOut> userOut = users.stream()
                .map(UserResponse.UsersOut.UserOut::new)
                .collect(Collectors.toList());
        return new UserResponse.UsersOut(userOut, users.isLast());
    }

    // 개인정보 조회
    public UserResponse.UserOut getUserInfo(Long id, User user) {
        User findUser = notFoundUser(id);
        forbiddenUser(findUser, user);
        return new UserResponse.UserOut(findUser);
    }

    // 개인정보 수정
    @Transactional
    public UserResponse.UserOut updateUserInfo(Long id, UserRequest.UserIn userIn, User user) {
        User findUser = notFoundUser(id);
        forbiddenUser(findUser, user);
        if (!userIn.getPassword().isEmpty()){
            passwordCheck(userIn.getPassword(), userIn.getPasswordCheck());
            findUser.pwdUpdate(passwordEncoder.encode(userIn.getPassword()));
        }
        if (!userIn.getProfileId().equals(1L)) {
            Profile profile = profileRepository.getReferenceById(userIn.getProfileId());
            findUser.update(userIn, profile);
        }else findUser.update(userIn);
        return new UserResponse.UserOut(findUser);
    }

    private User notFoundUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> new Exception404("해당 유저가 없습니다")
        );
    }

    private void validatePassword(String password, User user) {
        if (!passwordEncoder.matches(password, user.getPassword())) throw new Exception400("password", "비밀번호가 틀렸습니다");
    }

    private void passwordCheck(String password, String passwordCheck){
        if(!password.equals(passwordCheck)) throw new Exception400("password", "비밀번호가 틀렸습니다");
    }

    // 유저 권한 체크
    private void forbiddenUser(User findUser, User loginUser) {
        if (loginUser.getRole() == User.Role.USER && !findUser.getId().equals(loginUser.getId()))
            throw new Exception403("권한이 없습니다");
    }
}
