package com.task.needmoretask.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.task.needmoretask.core.auth.jwt.MyJwtProvider;
import com.task.needmoretask.dto.ResponseDTO;
import com.task.needmoretask.dto.task.TaskRequest;
import com.task.needmoretask.model.profile.Profile;
import com.task.needmoretask.model.profile.ProfileRepository;
import com.task.needmoretask.model.task.Task;
import com.task.needmoretask.model.user.User;
import com.task.needmoretask.model.user.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
class TaskControllerTest {

    @Autowired
    TestRestTemplate testRestTemplate;
    @Autowired
    UserRepository userRepository;

    private HttpHeaders headers(User user) {
        String jwt = MyJwtProvider.create(user);

        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        requestHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
        requestHeaders.add(MyJwtProvider.HEADER, jwt);

        return requestHeaders;
    }

    TaskRequest getTaskrequest(LocalDate start, LocalDate end, Long userId, String title, String desc, Task.Priority priority, Task.Progress progress) {
        List<TaskRequest.AssigneeRequest> assignees = List.of(TaskRequest.AssigneeRequest.builder().userId(userId).build());
        return TaskRequest.builder()
                .startAt(start)
                .endAt(end)
                .title(title)
                .desc(desc)
                .assignee(assignees)
                .priority(priority)
                .progress(progress)
                .build();
    }

    @BeforeEach
    void setUp(
            @Autowired ProfileRepository profileRepository
    ) {
        Profile profile = profileRepository.save(new Profile(null, "img.jpg"));
        User user = User.builder()
                .email("email@email.com")
                .password("1234")
                .phone("010-0000-0000")
                .fullname("test")
                .department(User.Department.HR)
                .joinCompanyYear(2023)
                .profile(profile)
                .role(User.Role.USER)
                .build();
        userRepository.save(user);
    }

    @Nested
    @DirtiesContext
    @DisplayName("Task 작성")
    class Create {
        @Test
        @DisplayName("성공")
        void createTask() throws JsonProcessingException {
            //given
            long userId = 1;
            User user = userRepository.findById(userId).orElse(null);
            HttpHeaders headers = headers(user);
            LocalDate start = LocalDate.of(2023, 5, 3);
            LocalDate end = LocalDate.of(2023, 6, 3);
            TaskRequest taskRequest = getTaskrequest(start, end, userId, "title", "description", Task.Priority.LOW, Task.Progress.IN_PROGRESS);
            HttpEntity<?> requestEntity = new HttpEntity<>(taskRequest, headers);

            //when
            ResponseEntity<?> response = testRestTemplate
                    .postForEntity(
                            "/api/task",
                            requestEntity,
                            ResponseDTO.class
                    );

            //then
            Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
            ObjectMapper om = new ObjectMapper();
            JsonNode jsonNode = om.readTree(om.writeValueAsString(response.getBody()));
            Assertions.assertEquals("성공", jsonNode.get("msg").asText());
        }
    }
}