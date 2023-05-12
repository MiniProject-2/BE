package com.task.needmoretask.service;

import com.task.needmoretask.core.auth.jwt.MyJwtProvider;
import com.task.needmoretask.core.exception.Exception400;
import com.task.needmoretask.core.exception.Exception401;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
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
                    if(!user.getEmail().equals(email)) throw new Exception401("로그인에 실패했습니다");
                    return Optional.of(user);
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
}