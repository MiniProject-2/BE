package com.task.needmoretask.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.task.needmoretask.core.auth.jwt.MyJwtProvider;
import com.task.needmoretask.dto.ResponseDTO;
import com.task.needmoretask.model.profile.Profile;
import com.task.needmoretask.model.profile.ProfileRepository;
import com.task.needmoretask.model.user.User;
import com.task.needmoretask.model.user.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
class UserControllerTest {

    @Autowired
    TestRestTemplate testRestTemplate;
    @Autowired
    UserRepository userRepository;
    private HttpHeaders headers(User user){
        String jwt = MyJwtProvider.create(user);

        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        requestHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
        requestHeaders.add(MyJwtProvider.HEADER, jwt);

        return requestHeaders;
    }
    long userId1,userId2;

    @BeforeEach
    void setUp(
            @Autowired ProfileRepository profileRepository
    ) {
        Profile profile = profileRepository.save(new Profile(null, "img.jpg"));
        List<User> users = new ArrayList<>();
        User user1 = User.builder()
                .email("user1@email.com")
                .password("1234")
                .phone("010-0000-0000")
                .fullname("user1")
                .department(User.Department.HR)
                .joinCompanyYear(2023)
                .profile(profile)
                .role(User.Role.USER)
                .build();
        User user2 = User.builder()
                .email("user2@email.com")
                .password("1234")
                .phone("010-0000-0000")
                .fullname("user2")
                .department(User.Department.DEVELOPMENT)
                .joinCompanyYear(2024)
                .profile(profile)
                .role(User.Role.USER)
                .build();
        users.add(user1);
        users.add(user2);
        for(int i=3; i<101; i++){
            users.add(User.builder()
                    .email("user"+i+"@email.com")
                    .password("1234")
                    .phone("010-0000-0000")
                    .fullname("user"+i)
                    .department(User.Department.HR)
                    .joinCompanyYear(2023)
                    .profile(profile)
                    .role(User.Role.USER)
                    .build());
        }
        userRepository.saveAll(users);
        userId1 = user1.getId();
        userId2 = user2.getId();
    }

    @Nested
    @DisplayName("User 조회")
    class GetUsers{
        @Test
        @DirtiesContext
        @DisplayName("성공")
        void getUsers() throws JsonProcessingException {
            //given
            int page = 0;
            User user = userRepository.findById(userId1).orElse(null);
            HttpHeaders headers = headers(user);
            HttpEntity<?> requestEntity = new HttpEntity<>(headers);

            //when
            ResponseEntity<?> response = testRestTemplate
                    .exchange(
                            "/api/admin/users?page="+page,
                            HttpMethod.GET,
                            requestEntity,
                            ResponseDTO.class
                    );

            //then
            Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
            ObjectMapper om = new ObjectMapper();
            JsonNode jsonNode = om.readTree(om.writeValueAsString(response.getBody()));
            Assertions.assertEquals("성공", jsonNode.get("msg").asText());
            JsonNode data = jsonNode.get("data");
            Assertions.assertEquals(10,data.get("users").size());
        }
    }

    @Nested
    @DisplayName("User 검색")
    class SearchUsers{
        @Test
        @DirtiesContext
        @DisplayName("성공")
        void searchUsers() throws JsonProcessingException {
            //given
            int page = 0;
            String fullName = "user10";
            User user = userRepository.findById(userId1).orElse(null);
            HttpHeaders headers = headers(user);
            HttpEntity<?> requestEntity = new HttpEntity<>(headers);

            //when
            ResponseEntity<?> response = testRestTemplate
                    .exchange(
                            "/api/users/search?fullName="+fullName+"&page="+page,
                            HttpMethod.GET,
                            requestEntity,
                            ResponseDTO.class
                    );

            //then
            Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
            ObjectMapper om = new ObjectMapper();
            JsonNode jsonNode = om.readTree(om.writeValueAsString(response.getBody()));
            Assertions.assertEquals("성공", jsonNode.get("msg").asText());
            JsonNode data = jsonNode.get("data");
            Assertions.assertEquals(2,data.get("users").size());
            Assertions.assertTrue(data.get("isLast").asBoolean());
        }
    }
}