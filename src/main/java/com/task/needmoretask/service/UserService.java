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

import javax.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;
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

    //회원가입
    @Transactional
    public void join(UserRequest.JoinIn joinIn) {

        // email 중복 검사
        if (userRepository.findUserByEmail(joinIn.getEmail()).isPresent()) {
            throw new Exception400("email", "이미 가입한 email이 있습니다");
        }

        // profile 유효성 검사
        Long profileId = joinIn.getProfileId();
        Profile profile = profileRepository.findById(profileId).orElseThrow(() -> new Exception400("profile", "일치하는 profile이 없습니다"));

        User user = new User(joinIn, profile, passwordEncoder.encode(joinIn.getPassword()));

        userRepository.save(user);
    }


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
        Auth auth = authRepository.findAuthByAccessToken(accessToken)
                .orElseGet(() -> authRepository.save(Auth.builder()
                        .userId(user.getId())
                        .accessToken(accessToken)
                        .build())
                );

        return auth.getAccessToken();
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

    public UserResponse.AllUsersOut getAllUsers(){
        List<User> users = userRepository.findAll();
        List<UserResponse.AllUsersOut.AllUserOut> userOut = users.stream()
                .map(UserResponse.AllUsersOut.AllUserOut::new)
                .collect(Collectors.toList());
        return new UserResponse.AllUsersOut(userOut);
    }

    //유저 조회
    public UserResponse.UsersOut getUsers(String role, Pageable pageable) {
        if(!role.equals("all") && !role.equals("admin") && !role.equals("user")) throw new Exception400("role","잘못된 요청입니다");
        Page<User> users = userRepository.findAll(pageable);
        if(!role.equals("all")) users = userRepository.findAllByRole(User.Role.valueOf(role.toUpperCase()), pageable);
        List<UserResponse.UsersOut.UserOut> userOut = users.stream()
                .map(UserResponse.UsersOut.UserOut::new)
                .collect(Collectors.toList());
        return new UserResponse.UsersOut(userOut, users.getTotalElements());
    }

    //유저 검색
    public UserResponse.UsersOut searchUsers(String fullName, Pageable pageable) {
        Page<User> users = userRepository.findUsersByFullName(fullName, pageable);
        List<UserResponse.UsersOut.UserOut> userOut = users.stream()
                .map(UserResponse.UsersOut.UserOut::new)
                .collect(Collectors.toList());
        return new UserResponse.UsersOut(userOut, users.getTotalElements());
    }

    // 개인정보 조회
    public UserResponse.UserOut getUserInfo(Long id) {
        User findUser = notFoundUser(id);
        return new UserResponse.UserOut(findUser);
    }

    // 개인정보 수정
    @Transactional
    public UserResponse.UserOut updateUserInfo(Long id, UserRequest.UserIn userIn, User user) {
        User findUser = notFoundUser(id);
        forbiddenUser(findUser, user);
        if (!userIn.getPassword().isEmpty()){
            if(!Pattern.matches("^[a-zA-Z0-9.-]{6,16}$",userIn.getPassword()) && !Pattern.matches("^[a-zA-Z0-9.-]{6,16}$",userIn.getPasswordCheck())) throw new Exception400("password","비밀번호 형식이 잘못 되었습니다");
            passwordCheck(userIn.getPassword(), userIn.getPasswordCheck());
            findUser.pwdUpdate(passwordEncoder.encode(userIn.getPassword()));
        }
        if (!userIn.getProfileId().equals(1L)) {
            Profile profile = profileRepository.getReferenceById(userIn.getProfileId());
            findUser.update(userIn, profile);
        }else findUser.update(userIn);
        return new UserResponse.UserOut(findUser);
    }

    //비밀번호 확인
    public void validatePassword(UserRequest.@Valid UserPasswordValidate userPasswordDTO, User user){
        User findUser = notFoundUser(user.getId());
        passwordCheck(userPasswordDTO.getPassword(), userPasswordDTO.getPasswordCheck());
        // match 원본 비번가 암호된 비번이 같은건지 비교가능하게 해주는 메서드
        validatePassword(userPasswordDTO.getPassword(),findUser);
    }

    //이메일 중복 확인
    public void isDuplicatedId(UserRequest.UserEmailValidate emailCheck){
        userRepository.findUserByEmail(emailCheck.getEmail()).ifPresent(u -> {throw new Exception400("email","이미 존재하는 이메일입니다");});
    }

    //정보 요청
    public UserResponse.UserOut getAuth(User user) {
        User findUser = userRepository.findById(user.getId()).orElseThrow(
                () -> new Exception401("잘못된 접근입니다")
        );
        return new UserResponse.UserOut(findUser);
    }

    // 권한 수정
    @Transactional
    public void updateRole(User loginUser, UserRequest.updateRoleInDTO updateRoleInDTO) {
        if (!loginUser.getRole().equals(User.Role.ADMIN))
            throw new Exception403("권한이 부족합니다");

        User userPS = userRepository.findById(updateRoleInDTO.getUserId())
                .orElseThrow(() -> new Exception400("userId", "잘못된 유저입니다"));

        userPS.updateRole(updateRoleInDTO.getRole());
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