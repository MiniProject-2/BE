package com.task.needmoretask.service;

import com.task.needmoretask.core.auth.jwt.MyJwtProvider;
import com.task.needmoretask.core.exception.Exception400;
import com.task.needmoretask.core.exception.Exception401;
import com.task.needmoretask.core.exception.Exception403;
import com.task.needmoretask.core.exception.Exception404;
import com.task.needmoretask.core.util.S3Uploader;
import com.task.needmoretask.dto.user.UserRequest;
import com.task.needmoretask.model.auth.AuthRepository;
import com.task.needmoretask.model.log.LogRepository;
import com.task.needmoretask.model.profile.Profile;
import com.task.needmoretask.model.profile.ProfileRepository;
import com.task.needmoretask.model.user.User;
import com.task.needmoretask.model.user.UserRepository;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;
    @Mock
    private ProfileRepository profileRepository;
    @Mock
    private LogRepository logRepository;
    @Mock
    private AuthRepository authRepository;
    @Mock
    private S3Uploader s3Uploader;
    @Mock
    private BCryptPasswordEncoder passwordEncoder;
    @Mock
    private MyJwtProvider myJwtProvider;

    private User user;
    private Profile profile;
    private final String path = "src/test/resources/images/";
    private String originalFilename = "default.jpeg";
    private final Pageable pageable = PageRequest.of(0,10);

    @BeforeEach
    void setUp() throws IOException {
        profile = Profile.builder().url(path+originalFilename).build();

        user = User.builder()
                .id(1L)
                .email("email@email.com")
                .password("123456")
                .phone("010-0000-0000")
                .fullname("test")
                .department(User.Department.HR)
                .joinCompanyYear(2023)
                .profile(profile)
                .role(User.Role.USER)
                .build();

        lenient().when(userRepository.findById(anyLong()))
                .thenAnswer(invocation -> {
                    Long userId = invocation.getArgument(0);
                    if (!user.getId().equals(userId)) throw new Exception404("유저를 찾을 수 없습니다");
                    return Optional.of(user);
                });

        lenient().when(userRepository.findUserByEmail(anyString()))
                .thenAnswer(invocation -> {
                    String email = invocation.getArgument(0);
                    if(!user.getEmail().equals(email)){
                        if(email.equals("email2@email.com")) return Optional.empty();
                        throw new Exception401("로그인에 실패했습니다");
                    }
                    return Optional.of(user);
                });

        lenient().when(userRepository.findAll(pageable))
                .thenAnswer(invocation -> new PageImpl<>(List.of(user),pageable,1));

        lenient().when(userRepository.findUsersByFullName(anyString(),any()))
                .thenAnswer(invocation -> {
                    String fullName = invocation.getArgument(0);
                    if(user.getFullname().equals(fullName)) return new PageImpl<>(List.of(user),pageable,1);
                    return Optional.empty();
                });

        lenient().when(passwordEncoder.matches(anyString(), anyString()))
                .thenAnswer(invocation -> {
                    String password = invocation.getArgument(0);
                    if(!user.getPassword().equals(password)) throw new Exception400("password", "비밀번호가 틀렸습니다");
                    return true;
                });

        lenient().when(s3Uploader.upload(any(),anyString()))
                .thenAnswer(invocation -> {
                    MultipartFile img = invocation.getArgument(0);
                    if(img.isEmpty()) throw new Exception400("profile", "이미지가 전송되지 않았습니다");
                    return path+originalFilename;
                });

        lenient().when(profileRepository.findById(anyLong()))
                .thenAnswer(invocation -> {
                    Long id = invocation.getArgument(0);
                    if(!id.equals(1L) && !id.equals(2L)) throw new Exception404("프로필이 없습니다");
                    return Optional.of(profile);
                });
    }

    @Nested
    @DisplayName("로그인")
    class Login{
        @Nested
        @DisplayName("실패")
        class Fail{
            @Test
            @DisplayName("1: 없는 유저")
            void test1(){
                //given
                UserRequest.Login request = UserRequest.Login.builder()
                        .email("test@email.com")
                        .password("123456")
                        .build();
                //when
                Assertions.assertThrows(Exception401.class, () -> userService.login(request,"",""));
            }
            @Test
            @DisplayName("2: 비밀번호 틀림")
            void test2(){
                //given
                UserRequest.Login request = UserRequest.Login.builder()
                        .email(user.getEmail())
                        .password("000000")
                        .build();
                //when
                Assertions.assertThrows(Exception400.class, () -> userService.login(request,"",""));
            }
        }
        @Test
        @DisplayName("성공")
        void success() {
            //given
            String pwd = "123456";
            UserRequest.Login request = UserRequest.Login.builder()
                    .email(user.getEmail())
                    .password(pwd)
                    .build();
            //when
            userService.login(request,"","");
            //then
            verify(userRepository, times(1)).findUserByEmail(user.getEmail());
            verify(passwordEncoder, times(1)).matches(pwd,user.getPassword());
            verify(logRepository, times(1)).findLogByEmail(user.getEmail());
            verify(myJwtProvider, times(1)).create(user);
            Assertions.assertDoesNotThrow(() -> userService.login(request, "", ""));
        }
    }

    @Nested
    @DisplayName("프로필 업로드")
    class UploadProfile{
        @Nested
        @DisplayName("실패")
        class Fail{
            @Test
            @DisplayName("1: 확장자가 다름")
            void test1() throws IOException {
                //given
                originalFilename = "fail.txt";
                String contentType = ContentType.TEXT_PLAIN.toString();
                byte[] content = Files.readAllBytes(Paths.get(path+originalFilename));
                MultipartFile image = new MockMultipartFile(originalFilename,originalFilename,contentType,content);
                //when then
                Assertions.assertThrows(Exception400.class, () -> userService.updateImage(image));
            }
        }
        @Test
        @DisplayName("성공")
        void success() throws IOException {
            //given
            String contentType = ContentType.IMAGE_JPEG.toString();
            byte[] content = Files.readAllBytes(Paths.get(path+originalFilename));
            MultipartFile image = new MockMultipartFile(originalFilename,originalFilename,contentType,content);
            //when
            userService.updateImage(image);
            //then
            verify(s3Uploader,times(1)).upload(image,"images");
            verify(profileRepository,times(1)).save(profile);
            Assertions.assertDoesNotThrow(() -> userService.updateImage(image));
        }
    }

    @Nested
    @DisplayName("유저 전체 조회")
    class AllUsers{
        @Test
        @DisplayName("성공")
        void success(){
            //given
            //when
            userService.getAllUsers();
            //then
            verify(userRepository,times(1)).findAll();
            Assertions.assertDoesNotThrow(() -> userService.getAllUsers());
        }
    }

    @Nested
    @DisplayName("유저 조회")
    class Users{
        @Nested
        @DisplayName("실패")
        class Fail{
            @Test
            @DisplayName("1: param 잘못 전달됨")
            void test1(){
                //given
                String role = "test";
                //when then
                Assertions.assertThrows(Exception400.class, () -> userService.getUsers(role,pageable));
            }
        }
        @Test
        @DisplayName("성공")
        void success(){
            //given
            String role = "all";
            //when
            userService.getUsers(role,pageable);
            //then
            verify(userRepository,times(1)).findAll(pageable);
            Assertions.assertDoesNotThrow(() -> userService.getUsers(role, pageable));
        }
    }

    @Nested
    @DisplayName("유저 검색")
    class SearchUsers{
        @Test
        @DisplayName("성공")
        void success(){
            //given
            String fullName = user.getFullname();
            //when
            userService.searchUsers(fullName,pageable);
            //then
            verify(userRepository,times(1)).findUsersByFullName(fullName,pageable);
            Assertions.assertDoesNotThrow(() -> userService.searchUsers(fullName, pageable));
        }
    }

    @Nested
    @DisplayName("개인정보 조회")
    class UserInfo {
        @Test
        @DisplayName("실패: 유저 없음")
        void test() {
            //given
            Long id = 2L;
            //when then
            Assertions.assertThrows(Exception404.class, () -> userService.getUserInfo(id));
        }

        @Test
        @DisplayName("성공")
        void success() {
            //given
            Long id = user.getId();
            //when
            userService.getUserInfo(id);
            //then
            verify(userRepository, times(1)).findById(id);
            Assertions.assertDoesNotThrow(() -> userService.getUserInfo(id));
        }
    }

    @Nested
    @DisplayName("개인정보 수정")
    class UpdateUserInfo {
        @Nested
        @DisplayName("실패")
        class Fail {
            @Nested
            @DisplayName("1: 비밀번호 변경")
            class Password {
                @Test
                @DisplayName("1-1: 비밀번호 확인 다름")
                void test1() {
                    //given
                    Long id = user.getId();
                    String passwordCheck = "000000";
                    Long profileId = 1L;
                    UserRequest.UserIn request = UserRequest.UserIn.builder()
                            .password(user.getPassword())
                            .passwordCheck(passwordCheck)
                            .phone(user.getPhone())
                            .fullName(user.getFullname())
                            .department(user.getDepartment())
                            .joinCompanyYear(user.getJoinCompanyYear())
                            .profileId(profile.getId())
                            .build();
                    //when then
                    Assertions.assertThrows(Exception400.class, () -> userService.updateUserInfo(id, request, user));
                }

                @Test
                @DisplayName("1-2: 비밀번호 정규식 다름")
                void test2() {
                    //given
                    Long id = user.getId();
                    String password = "123";
                    Long profileId = 1L;
                    UserRequest.UserIn request = UserRequest.UserIn.builder()
                            .password(password)
                            .passwordCheck(password)
                            .phone(user.getPhone())
                            .fullName(user.getFullname())
                            .department(user.getDepartment())
                            .joinCompanyYear(user.getJoinCompanyYear())
                            .profileId(profile.getId())
                            .build();
                    //when then
                    Assertions.assertThrows(Exception400.class, () -> userService.updateUserInfo(id, request, user));
                }
            }

            @Test
            @DisplayName("2: 프로필 없음")
            void test(){
                //given
                Long id = user.getId();
                String password = user.getPassword();
                Long profileId = 3L;
                UserRequest.UserIn request = UserRequest.UserIn.builder()
                        .password(password)
                        .passwordCheck(password)
                        .phone(user.getPhone())
                        .fullName(user.getFullname())
                        .department(user.getDepartment())
                        .joinCompanyYear(user.getJoinCompanyYear())
                        .profileId(profileId)
                        .build();
                //when then
                Assertions.assertThrows(Exception404.class, () -> userService.updateUserInfo(id, request, user));
            }

            @Test
            @DisplayName("3: 권한 없음")
            void test3(){
                //given
                User user1 = User.builder().id(2L).role(User.Role.USER).build();
                String password = user.getPassword();
                Long profileId = 1L;
                UserRequest.UserIn request = UserRequest.UserIn.builder()
                        .password(password)
                        .passwordCheck(password)
                        .phone(user.getPhone())
                        .fullName(user.getFullname())
                        .department(user.getDepartment())
                        .joinCompanyYear(user.getJoinCompanyYear())
                        .profileId(profileId)
                        .build();
                //when then
                Assertions.assertThrows(Exception403.class, () -> userService.updateUserInfo(user.getId(),request,user1));
            }
        }

        @Nested
        @DisplayName("성공")
        class Success {
            @Test
            @DisplayName("비밀번호, 프로필 변경x")
            void test1() {
                //given
                Long id = user.getId();
                Long profileId = 1L;
                UserRequest.UserIn request = UserRequest.UserIn.builder()
                        .password("")
                        .passwordCheck("")
                        .phone(user.getPhone())
                        .fullName(user.getFullname())
                        .department(user.getDepartment())
                        .joinCompanyYear(user.getJoinCompanyYear())
                        .profileId(profileId)
                        .build();
                //when
                userService.updateUserInfo(id,request,user);
                //then
                verify(passwordEncoder, times(0)).encode("");
                verify(profileRepository, times(0)).findById(profileId);
                Assertions.assertDoesNotThrow(() -> userService.updateUserInfo(id, request, user));
            }
            @Test
            @DisplayName("비밀번호 변경")
            void test2() {
                //given
                Long id = user.getId();
                String password = "000000";
                Long profileId = 1L;
                UserRequest.UserIn request = UserRequest.UserIn.builder()
                        .password(password)
                        .passwordCheck(password)
                        .phone(user.getPhone())
                        .fullName(user.getFullname())
                        .department(user.getDepartment())
                        .joinCompanyYear(user.getJoinCompanyYear())
                        .profileId(profileId)
                        .build();
                //when
                userService.updateUserInfo(id,request,user);
                //then
                verify(passwordEncoder, times(1)).encode(password);
                verify(profileRepository, times(0)).findById(profileId);
                Assertions.assertDoesNotThrow(() -> userService.updateUserInfo(id, request, user));
            }
            @Test
            @DisplayName("프로필 변경")
            void test3() {
                //given
                Long id = user.getId();
                Long profileId = 2L;
                UserRequest.UserIn request = UserRequest.UserIn.builder()
                        .password("")
                        .passwordCheck("")
                        .phone(user.getPhone())
                        .fullName(user.getFullname())
                        .department(user.getDepartment())
                        .joinCompanyYear(user.getJoinCompanyYear())
                        .profileId(profileId)
                        .build();
                //when
                userService.updateUserInfo(id,request,user);
                //then
                verify(passwordEncoder, times(0)).encode("");
                verify(profileRepository,times(1)).findById(profileId);
                Assertions.assertDoesNotThrow(() -> userService.updateUserInfo(id, request, user));
            }
            @Test
            @DisplayName("비밀번호, 프로필 변경")
            void test4() {
                //given
                Long id = user.getId();
                String password = "000000";
                Long profileId = 2L;
                UserRequest.UserIn request = UserRequest.UserIn.builder()
                        .password(password)
                        .passwordCheck(password)
                        .phone(user.getPhone())
                        .fullName(user.getFullname())
                        .department(user.getDepartment())
                        .joinCompanyYear(user.getJoinCompanyYear())
                        .profileId(profileId)
                        .build();
                //when
                userService.updateUserInfo(id,request,user);
                //then
                verify(passwordEncoder, times(1)).encode(password);
                verify(profileRepository, times(1)).findById(profileId);
                Assertions.assertDoesNotThrow(() -> userService.updateUserInfo(id, request, user));
            }
        }
    }

    @Nested
    @DisplayName("정보 요청")
    class GetAuth{
        @Test
        @DisplayName("실패: 유저가 다름")
        void fail(){
            //given
            User user1 = User.builder().id(2L).build();
            //when then
            Assertions.assertThrows(Exception404.class, () -> userService.getAuth(user1));
        }
        @Test
        @DisplayName("성공")
        void success(){
            //given
            //when
            userService.getAuth(user);
            //then
            verify(userRepository,times(1)).findById(user.getId());
            Assertions.assertDoesNotThrow(() -> userService.getAuth(user));
        }
    }

    @Nested
    @DisplayName("비밀번호 확인")
    class Validate {

        @Nested
        @DisplayName("실패 케이스")
        class Fail {
            @Test
            @DisplayName("1:해당유저 없음")
            void noUser() {
                //given
                User user1 = User.builder()
                        .id(2L)
                        .build();
                UserRequest.UserPasswordValidate userPasswordDto = UserRequest.UserPasswordValidate.builder()
                        .password("hello1234")
                        .passwordCheck("hello1234")
                        .build();
                //when
                Assertions.assertThrows(Exception404.class, () -> userService.validatePassword(userPasswordDto, user1));
            }

            @Test
            @DisplayName("2:passwordCheck() 실패")
                //입력한 두값이 같지 않을때 실패 케이스
            void passwordCheck() {
                //given
                UserRequest.UserPasswordValidate userPasswordDto = UserRequest.UserPasswordValidate.builder()
                        .password("123456")
                        .passwordCheck("1234567")
                        .build();
                //then
                Assertions.assertThrows(Exception400.class, () -> userService.validatePassword(userPasswordDto, user));
            }

            @Test
            @DisplayName("3:validatePassword() 실패")
                //입력한 두값이 확인후 DB에 저장된 UserPassword 같지않을 경우
            void validatePassword() {
                //given
                UserRequest.UserPasswordValidate userPasswordDto = UserRequest.UserPasswordValidate.builder()
                        .password("hello123")
                        .passwordCheck("hello123")
                        .build();
                //when
                Exception400 failPassword = Assertions.assertThrows(Exception400.class, () -> userService.validatePassword(userPasswordDto, user));
                //then
                Assertions.assertThrows(Exception400.class, () -> userService.validatePassword(userPasswordDto, user));
            }
        }

        @Test
        @DisplayName("비밀번호 확인 성공")
        void success() {
            //given
            UserRequest.UserPasswordValidate userPasswordDto = UserRequest.UserPasswordValidate.builder()
                    .password("123456")
                    .passwordCheck("123456")
                    .build();
            //when then
            Assertions.assertDoesNotThrow(() -> userService.validatePassword(userPasswordDto, user));
        }
    }

    @Nested
    @DisplayName("이메일 중복확인")
    class isDuplicatedId {

        @Test
        @DisplayName("이메일 중복되었을때")
        void duplicatedEmail() {
            //given
            String email = "email@email.com";
            UserRequest.UserEmailValidate emailCheck = UserRequest.UserEmailValidate.builder()
                    .email(email)
                    .build();
            //then
            Assertions.assertThrows(Exception400.class, () -> userService.isDuplicatedId(emailCheck));
        }

        @Test
        @DisplayName("이메일 중복되지않았을때")
        void notDuplicatedEmail(){
            //given
            UserRequest.UserEmailValidate emailCheck= new UserRequest.UserEmailValidate("email2@email.com");

            //then
            Assertions.assertDoesNotThrow(() -> userService.isDuplicatedId(emailCheck));
        }
    }
}