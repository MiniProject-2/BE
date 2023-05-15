package com.task.needmoretask.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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

    @Autowired
    MyJwtProvider myJwtProvider;

    private HttpHeaders headers(User user){
        String jwt = myJwtProvider.create(user);

        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        requestHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
        requestHeaders.add(MyJwtProvider.HEADER, jwt);

        return requestHeaders;
    }

    TaskRequest getTaskrequest(LocalDate start, LocalDate end, Long userId, String title, String desc, Task.Priority priority, Task.Progress progress) {
        List<TaskRequest.AssigneeRequest> assignees = new ArrayList<>();
        if(userId != null) assignees = List.of(TaskRequest.AssigneeRequest.builder().userId(userId).build());
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

    long userId1,userId2,taskId,taskId2;

    @BeforeEach
    void setUp(
            @Autowired ProfileRepository profileRepository
    ) {
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
                .role(User.Role.ADMIN)
                .build();
        User user3 = User.builder()
                .email("user3@email.com")
                .password("1234")
                .phone("010-0000-0000")
                .fullname("user3")
                .department(User.Department.DEVELOPMENT)
                .joinCompanyYear(2024)
                .profile(profile)
                .role(User.Role.ADMIN)
                .build();
        List<User> users = List.of(user1,user2,user3);
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
        taskRepository.save(task);
        assignRepository.saveAll(assginees);
        taskId = task.getId();

        Task task2 = getTaskrequest(start,end,null,"title","desc", Task.Priority.LOW, Task.Progress.TODO).toEntity(user1);
        taskRepository.save(task2);
        taskId2 = task2.getId();

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

            taskRepository.save(task1);
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
                    .exchange(
                            "/api/task/" + taskId,
                            HttpMethod.PUT,
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
                    .exchange(
                            "/api/task/" + taskId,
                            HttpMethod.DELETE,
                            requestEntity,
                            ResponseDTO.class
                    );

            //then
            Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
            ObjectMapper om = new ObjectMapper();
            JsonNode jsonNode = om.readTree(om.writeValueAsString(response.getBody()));
            JsonNode data = jsonNode.get("data");
        }
    }

    @Nested
    @DisplayName("Task 상세보기")
    class Detail {

        @Test
        @DirtiesContext
        @DisplayName("성공1: assignee 있음")
        void getDetailTask1() throws JsonProcessingException {
            //given
            User user = userRepository.findById(userId1).orElse(null);
            HttpHeaders headers = headers(user);
            HttpEntity<?> requestEntity = new HttpEntity<>(headers);

            //when
            ResponseEntity<?> response = testRestTemplate
                    .exchange(
                            "/api/task/" + taskId,
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
            Assertions.assertEquals(userId1,data.get("taskOwner").get("userId").asLong());
            Assertions.assertFalse(data.get("assignee").isEmpty());
        }

        @Test
        @DirtiesContext
        @DisplayName("성공2: assignee 없음")
        void getDetailTask2() throws JsonProcessingException {
            //given
            User user = userRepository.findById(userId1).orElse(null);
            HttpHeaders headers = headers(user);
            HttpEntity<?> requestEntity = new HttpEntity<>(headers);

            //when
            ResponseEntity<?> response = testRestTemplate
                    .exchange(
                            "/api/task/" + taskId2,
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
            Assertions.assertEquals(userId1,data.get("taskOwner").get("userId").asLong());
            Assertions.assertTrue(data.get("assignee").isEmpty());
        }
    }

    @Test
    @DisplayName("최신 생성 Task 7개")
    @DirtiesContext
    void getLatestTasks() {
        //given
        User user = userRepository.findById(userId1).orElse(null);
        HttpHeaders headers = headers(user);
        HttpEntity<?> requestEntity = new HttpEntity<>(headers);

        // when
        ResponseEntity<ResponseDTO> response = testRestTemplate
                .exchange(
                        "/api/tasks/latest",
                        HttpMethod.GET,
                        requestEntity,
                        ResponseDTO.class
                );

        // then
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("최근 2주간의 task, done 갯수")
    @DirtiesContext
    void getPerfomance() throws JsonProcessingException {
        //given
        User user = userRepository.findById(userId1).orElse(null);
        HttpHeaders headers = headers(user);
        HttpEntity<?> requestEntity = new HttpEntity<>(headers);
        //when
        ResponseEntity<ResponseDTO> response = testRestTemplate
                .exchange(
                        "/api/performance",
                        HttpMethod.GET,
                        requestEntity,
                        ResponseDTO.class
                );
        LocalDate localDate = LocalDate.now().minusWeeks(2).plusDays(1);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        ObjectMapper om = new ObjectMapper();
        JsonNode jsonNode = om.readTree(om.writeValueAsString(response.getBody()));
        JsonNode data = jsonNode.get("data");

        Assertions.assertEquals(localDate, LocalDate.parse(data.get(0).get("date").asText(), DateTimeFormatter.ISO_DATE));
        Assertions.assertEquals(0, data.get(0).get("taskCount").asInt());
        Assertions.assertEquals(0, data.get(0).get("doneCount").asInt());
    }

    @Test
    @DisplayName("최근 1주동안의 통계 데이터")
    @DirtiesContext
    void getProgress() throws JsonProcessingException {
        //given
        User user = userRepository.findById(userId1).orElse(null);
        HttpHeaders headers = headers(user);
        HttpEntity<?> requestEntity = new HttpEntity<>(headers);
        //when
        ResponseEntity<ResponseDTO> response = testRestTemplate
                .exchange(
                        "/api/progress",
                        HttpMethod.GET,
                        requestEntity,
                        ResponseDTO.class
                );

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        ObjectMapper om = new ObjectMapper();
        JsonNode jsonNode = om.readTree(om.writeValueAsString(response.getBody()));
        Assertions.assertEquals("성공", jsonNode.get("msg").asText());
        JsonNode data = jsonNode.get("data");
    }

    @Test
    @DisplayName("[Kanban] 내가 속한 task 조회")
    @DirtiesContext
    void getKanbans() throws JsonProcessingException {
        //given
        Long userid = 1L;
        User user = userRepository.findById(userid).orElse(null);
        HttpHeaders headers = headers(user);
        HttpEntity<?> requestEntity = new HttpEntity<>(headers);

        //when
        ResponseEntity<?> response = testRestTemplate
                .exchange(
                        "/api/kanbans",
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
        Assertions.assertEquals(userid,data.get(0).get("taskOwner").get("userId").asLong());
//        Assertions.assertEquals(1 ,data.get(0).get("assignee").size());
    }

    @Test
    @DisplayName("[Calendar] 조회")
    @DirtiesContext
    void getCalendar() throws JsonProcessingException {
        //given
        User user = userRepository.findById(userId1).orElse(null);
        HttpHeaders headers = headers(user);
        String url = "/api/calendars";
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url)
                .queryParam("year", "2023")
                .queryParam("month", "5");
        HttpEntity<?> requestEntity = new HttpEntity<>(headers);
        //when

        ResponseEntity<ResponseDTO> response = testRestTemplate
                .exchange(
                        builder.toUriString(),
                        HttpMethod.GET,
                        requestEntity,
                        ResponseDTO.class
                );

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        ObjectMapper om = new ObjectMapper();
        JsonNode jsonNode = om.readTree(om.writeValueAsString(response.getBody()));
        Assertions.assertEquals("성공", jsonNode.get("msg").asText());
        JsonNode data = jsonNode.get("data");

        Assertions.assertEquals(8, data.size());
    }

    @Test
    @DisplayName("Daily Overview")
    @DirtiesContext
    void getDailyTasks() throws JsonProcessingException {
        //given
        User user = userRepository.findById(userId1).orElse(null);
        HttpHeaders headers = headers(user);
        HttpEntity<?> requestEntity = new HttpEntity<>(headers);
        //when
        String url = "/api/tasks";
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url)
                .queryParam("date", "2023-04-01")
                .queryParam("page", 0);

        ResponseEntity<ResponseDTO> response = testRestTemplate
                .exchange(
                        builder.toUriString(),
                        HttpMethod.GET,
                        requestEntity,
                        ResponseDTO.class
                );

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        ObjectMapper om = new ObjectMapper();
        JsonNode jsonNode = om.readTree(om.writeValueAsString(response.getBody()));
        Assertions.assertEquals("성공", jsonNode.get("msg").asText());
        JsonNode data = jsonNode.get("data");

//        Assertions.assertEquals(1, data.size());
//        Assertions.assertEquals(0, data.get("totalCount"));
    }

    @Test
    @DisplayName("Admin Overview")
    @DirtiesContext
    void getPickedTasks() throws JsonProcessingException {
        //given
        String url = "/api/tasks/period";
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url)
                .queryParam("startat", "2023-01-07")
                .queryParam("endat", "2023-04-07")
                .queryParam("page", 0);
        User user = userRepository.findById(userId2).orElse(null);
        HttpHeaders headers = headers(user);
        HttpEntity<?> requestEntity = new HttpEntity<>(headers);

        //when
        ResponseEntity<?> response = testRestTemplate
                .exchange(
                        builder.toUriString(),
                        HttpMethod.GET,
                        requestEntity,
                        ResponseDTO.class
                );

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        ObjectMapper om = new ObjectMapper();
        JsonNode jsonNode = om.readTree(om.writeValueAsString(response.getBody()));
        Assertions.assertEquals("성공", jsonNode.get("msg").asText());
        JsonNode data = jsonNode.get("data");

//        Assertions.assertEquals(1, data.size());
    }
}