package com.task.needmoretask.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.task.needmoretask.core.auth.jwt.MyJwtProvider;
import com.task.needmoretask.core.exception.Exception404;
import com.task.needmoretask.dto.ResponseDTO;
import com.task.needmoretask.dto.task.TaskRequest;
import com.task.needmoretask.model.assign.AssignRepository;
import com.task.needmoretask.model.assign.Assignment;
import com.task.needmoretask.model.profile.Profile;
import com.task.needmoretask.model.profile.ProfileRepository;
import com.task.needmoretask.model.task.Task;
import com.task.needmoretask.model.task.TaskRepository;
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
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
class TaskControllerTest {

    @Autowired
    TestRestTemplate testRestTemplate;
    @Autowired
    UserRepository userRepository;
    @Autowired
    TaskRepository taskRepository;
    @Autowired
    AssignRepository assignRepository;

    private HttpHeaders headers(User user){
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

    long userId1,userId2,taskId;

    @BeforeEach
    void setUp(
            @Autowired ProfileRepository profileRepository
    ) throws JsonProcessingException {
        Profile profile = profileRepository.save(new Profile(null, "img.jpg"));
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
        List<User> users = List.of(user1,user2);
        userRepository.saveAll(users);
        userId1 = user1.getId();
        userId2 = user2.getId();

        LocalDate start = LocalDate.of(2023, 3, 3);
        LocalDate end = LocalDate.of(2023, 4, 3);
        TaskRequest taskRequest = getTaskrequest(start,end,user1.getId(),"수정 전 제목","수정 전 내용", Task.Priority.LOW, Task.Progress.IN_PROGRESS);
        Task task = taskRequest.toEntity(user1);
        List<Assignment> assginees = taskRequest.getAssignee().stream()
                .map(assigneeRequest -> {
                    User assignee = userRepository.findById(assigneeRequest.getUserId())
                            .orElseThrow(() -> new Exception404("유저를 찾을 수 없습니다"));
                    return Assignment.builder()
                            .user(assignee)
                            .task(task)
                            .build();
                })
                .collect(Collectors.toList());
        assignRepository.saveAll(assginees);
        taskId = task.getId();
        userRepository.save(user1);

        ObjectMapper om = new ObjectMapper();
        om.registerModule(new JavaTimeModule());
        JsonNode jsonNode = om.readTree(om.writeValueAsString(task));
        System.out.println(jsonNode);

        // given
        long userId = 1;
        User userPS = userRepository.findById(userId).orElse(null);
        LocalDate startAt = LocalDate.of(2023, 5, 3);
        LocalDate endAt = LocalDate.of(2023, 6, 3);

        for (int i = 0; i < 8; i++) {
            Task task1 = Task.builder()
                    .user(userPS)
                    .title("title"+i)
                    .description("desc"+i)
                    .startAt(startAt)
                    .endAt(endAt)
                    .progress(Task.Progress.DONE)
                    .priority(Task.Priority.LOW)
                    .build();

            Assignment assignment = Assignment.builder()
                    .user(userPS)
                    .task(task1)
                    .build();

            assignRepository.save(assignment);
        }

    }

    @Nested
    @DisplayName("Task 작성")
    class Create {
        @Test
        @DirtiesContext
        @DisplayName("성공")
        void createTask() throws JsonProcessingException {
            //given
            User user = userRepository.findById(userId1).orElse(null);
            HttpHeaders headers = headers(user);
            LocalDate start = LocalDate.of(2023, 5, 3);
            LocalDate end = LocalDate.of(2023, 6, 3);
            TaskRequest taskRequest = getTaskrequest(start, end, userId1, "title", "description", Task.Priority.LOW, Task.Progress.IN_PROGRESS);
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

    @Nested
    @DisplayName("Task 수정")
    class Update {

        @Test
        @DirtiesContext
        @DisplayName("성공")
        void updateTask() throws JsonProcessingException {
            //given
            User user = userRepository.findById(userId1).orElse(null);
            HttpHeaders headers = headers(user);
            LocalDate start = LocalDate.of(2023, 5, 3);
            LocalDate end = LocalDate.of(2023, 6, 3);
            String title = "수정 후 제목";
            String desc = "수정 후 내용";
            Task.Priority priority = Task.Priority.HIGH;
            Task.Progress progress = Task.Progress.DONE;
            TaskRequest taskRequest = getTaskrequest(start, end, userId2, title, desc, priority, progress);
            HttpEntity<?> requestEntity = new HttpEntity<>(taskRequest, headers);

            //when
            ResponseEntity<?> response = testRestTemplate
                    .postForEntity(
                            "/api/task/" + taskId + "/update",
                            requestEntity,
                            ResponseDTO.class
                    );

            //then
            Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
            ObjectMapper om = new ObjectMapper();
            JsonNode jsonNode = om.readTree(om.writeValueAsString(response.getBody()));
            JsonNode data = jsonNode.get("data");
            Assertions.assertEquals(taskId, data.get("id").asLong());
            Assertions.assertEquals(userId1, data.get("taskOwner").asLong());
            Assertions.assertEquals(title, data.get("title").asText());
            Assertions.assertEquals(desc, data.get("description").asText());
            System.out.println(data);
        }
    }

    @Nested
    @DisplayName("Task 삭제")
    class Delete {

        @Test
        @DirtiesContext
        @DisplayName("성공")
        void deleteTask() throws JsonProcessingException {
            //given
            User user = userRepository.findById(userId1).orElse(null);
            HttpHeaders headers = headers(user);
            HttpEntity<?> requestEntity = new HttpEntity<>(headers);

            //when
            ResponseEntity<?> response = testRestTemplate
                    .postForEntity(
                            "/api/task/" + taskId + "/delete",
                            requestEntity,
                            ResponseDTO.class
                    );

            //then
            Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
            ObjectMapper om = new ObjectMapper();
            JsonNode jsonNode = om.readTree(om.writeValueAsString(response.getBody()));
            JsonNode data = jsonNode.get("data");
            System.out.println(data);
        }
    }

    @Test
    @DirtiesContext
    void getLatestTasks() {

        // when
        ResponseEntity<ResponseDTO> response = testRestTemplate
                .getForEntity(
                        "/api/tasks/latest",
                        ResponseDTO.class
                );

        System.out.println(response.getBody().getData().toString());
        // then
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DirtiesContext
    void getPerfomance() throws JsonProcessingException {

        ResponseEntity<ResponseDTO> response = testRestTemplate
                .getForEntity(
                        "/api/performance",
                        ResponseDTO.class
                );

        LocalDate localDate = LocalDate.of(2023, 4, 22);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        ObjectMapper om = new ObjectMapper();
        JsonNode jsonNode = om.readTree(om.writeValueAsString(response.getBody()));
        JsonNode data = jsonNode.get("data");

        System.out.println(data.toString());

        Assertions.assertEquals(localDate, LocalDate.parse(data.get(0).get("date").asText(), DateTimeFormatter.ISO_DATE));
        Assertions.assertEquals(0, data.get(0).get("taskCount").asInt());
        Assertions.assertEquals(0, data.get(0).get("doneCount").asInt());
    }
}