package com.task.needmoretask.service;

import com.task.needmoretask.core.exception.Exception404;
import com.task.needmoretask.dto.task.TaskRequest;
import com.task.needmoretask.model.assign.AssignRepository;
import com.task.needmoretask.model.assign.Assignment;
import com.task.needmoretask.model.profile.Profile;
import com.task.needmoretask.model.task.Task;
import com.task.needmoretask.model.task.TaskJPQLRepository;
import com.task.needmoretask.model.task.TaskRepository;
import com.task.needmoretask.model.user.User;
import com.task.needmoretask.model.user.UserRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class TaskServiceTest {

    @InjectMocks
    private TaskService taskService;

    @Mock
    private TaskJPQLRepository taskJPQLRepository;
    @Mock
    private TaskRepository taskRepository;
    @Mock
    private AssignRepository assignRepository;
    @Mock
    private UserRepository userRepository;

    User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .email("email@email.com")
                .password("1234")
                .phone("010-0000-0000")
                .fullname("test")
                .department(User.Department.HR)
                .joinCompanyYear(2023)
                .profile(new Profile())
                .role(User.Role.USER)
                .build();
        when(userRepository.findById(anyLong()))
                .thenAnswer(invocation -> {
                    Long userId = invocation.getArgument(0);
                    if (!user.getId().equals(userId)) throw new Exception404("유저를 찾을 수 없습니다");
                    return Optional.of(user);
                });
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

    List<Assignment> getAssign(TaskRequest request) {
        return List.of(Assignment.builder()
                .user(user)
                .task(request.toEntity(user))
                .build());
    }

    @Nested
    @DisplayName("Task 작성")
    class Create {
        @Test
        @DisplayName("Assignee 유저를 찾을 수 없음")
        void fail() {
            //given
            LocalDate start = LocalDate.of(2023, 5, 3);
            LocalDate end = LocalDate.of(2023, 6, 3);
            TaskRequest request = getTaskrequest(start, end, 2L, "title", "description", Task.Priority.LOW, Task.Progress.IN_PROGRESS);
            //when then
            Assertions.assertThrows(Exception404.class, () -> taskService.createTask(request, user));
        }

        @Test
        @DisplayName("성공")
        void success() {
            //given
            LocalDate start = LocalDate.of(2023, 5, 3);
            LocalDate end = LocalDate.of(2023, 6, 3);
            TaskRequest request = getTaskrequest(start, end, 1L, "title", "description", Task.Priority.LOW, Task.Progress.IN_PROGRESS);
            List<Assignment> assignments = getAssign(request);
            //when
            taskService.createTask(request, user);
            //then
            verify(assignRepository, times(1)).saveAll(assignments);
            Assertions.assertDoesNotThrow(() -> taskService.createTask(request, user));
        }
    }
}