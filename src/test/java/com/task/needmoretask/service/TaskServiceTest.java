package com.task.needmoretask.service;

import com.task.needmoretask.core.exception.Exception403;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    private User user;
    private Task task;
    private ZonedDateTime date;

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
        lenient().when(userRepository.findById(anyLong()))
                .thenAnswer(invocation -> {
                    Long userId = invocation.getArgument(0);
                    if (!user.getId().equals(userId)) throw new Exception404("유저를 찾을 수 없습니다");
                    return Optional.of(user);
                });
        task = Task.builder()
                .id(1L)
                .startAt(LocalDate.of(2023, 4, 1))
                .endAt(LocalDate.of(2023, 5, 1))
                .title("title")
                .description("description")
                .priority(Task.Priority.LOW)
                .progress(Task.Progress.IN_PROGRESS)
                .user(user)
                .build();
        lenient().when(taskRepository.findById(anyLong()))
                .thenAnswer(invocation -> {
                    Long taskId = invocation.getArgument(0);
                    if (!task.getId().equals(taskId)) throw new Exception404("Task를 찾을 수 없습니다");
                    return Optional.of(task);
                });

        lenient().when(taskJPQLRepository.findLatestTasks())
                .thenReturn(List.of(task));

        lenient().when(taskJPQLRepository.countByProgress(LocalDate.of(2023,4,1), LocalDate.of(2023,4,1)))
                .thenReturn(new Long[]{3L, 1L, 4L});

    }

    TaskRequest getTaskrequest(Long assignId) {
        List<TaskRequest.AssigneeRequest> assignees = List.of(TaskRequest.AssigneeRequest.builder().userId(assignId).build());
        return TaskRequest.builder()
                .startAt(task.getStartAt())
                .endAt(task.getEndAt())
                .title(task.getTitle())
                .desc(task.getDescription())
                .assignee(assignees)
                .priority(task.getPriority())
                .progress(task.getProgress())
                .build();
    }

    @Nested
    @DisplayName("Task 작성")
    class Create {
        @Test
        @DisplayName("Assignee 유저를 찾을 수 없음")
        void fail() {
            //given
            TaskRequest request = getTaskrequest(2L);
            //when then
            Assertions.assertThrows(Exception404.class, () -> taskService.createTask(request, user));
        }

        @Test
        @DisplayName("성공")
        void success() {
            //given
            TaskRequest request = getTaskrequest(user.getId());
            List<Assignment> assignments = request.getAssignee().stream()
                    .map(assign -> Assignment.builder()
                            .task(request.toEntity(user))
                            .user(user)
                            .build())
                    .collect(Collectors.toList());
            //when
            taskService.createTask(request, user);
            //then
            verify(assignRepository, times(1)).saveAll(assignments);
            Assertions.assertDoesNotThrow(() -> taskService.createTask(request, user));
        }
    }

    @Nested
    @DisplayName("Task 수정")
    class Update {
        @Nested
        @DisplayName("실패")
        class Fail {
            @Test
            @DisplayName("1: Task 없음")
            void test1() {
                //given
                long taskId = 2;
                TaskRequest request = getTaskrequest(1L);
                //when then
                Assertions.assertThrows(Exception404.class, () -> taskService.updateTask(taskId, request, user));
            }

            @Test
            @DisplayName("2: 권한 없음")
            void test2() {
                //given
                long taskId = 1;
                User user1 = User.builder().id(2L).role(User.Role.USER).build();
                TaskRequest request = getTaskrequest(1L);
                //when then
                Assertions.assertThrows(Exception403.class, () -> taskService.updateTask(taskId, request, user1));
            }

            @Test
            @DisplayName("3: Assignee 유저를 찾을 수 없음")
            void test() {
                //given
                long taskId = 1;
                TaskRequest request = getTaskrequest(2L);
                //when then
                Assertions.assertThrows(Exception404.class, () -> taskService.updateTask(taskId, request, user));
            }
        }

        @Nested
        @DisplayName("성공")
        class Success {
            @Test
            @DisplayName("1: user 본인")
            void test1() {
                //given
                long taskId = 1;
                TaskRequest request = getTaskrequest(1L);
                //when
                taskService.updateTask(taskId, request, user);
                //then
                verify(taskRepository, times(1)).findById(taskId);
                verify(assignRepository, times(1)).findAssigneeByTaskId(taskId);
                Assertions.assertDoesNotThrow(() -> taskService.updateTask(taskId, request, user));
            }

            @Test
            @DisplayName("2: admin")
            void test2() {
                //given
                long taskId = 1;
                User admin = User.builder().id(2L).role(User.Role.ADMIN).build();
                TaskRequest request = getTaskrequest(1L);
                //when
                taskService.updateTask(taskId, request, admin);
                //then
                verify(taskRepository, times(1)).findById(taskId);
                verify(assignRepository, times(1)).findAssigneeByTaskId(taskId);
                Assertions.assertDoesNotThrow(() -> taskService.updateTask(taskId, request, admin));
            }
        }
    }

    @Nested
    @DisplayName("Task 삭제")
    class Delete {
        @Nested
        @DisplayName("실패")
        class Fail {
            @Test
            @DisplayName("1: Task 없음")
            void test1() {
                //given
                long taskId = 2;
                //when then
                Assertions.assertThrows(Exception404.class, () -> taskService.deleteTask(taskId, user));
            }

            @Test
            @DisplayName("2: 권한 없음")
            void test() {
                //given
                long taskId = 1;
                User user1 = User.builder().id(2L).role(User.Role.USER).build();
                //when then
                Assertions.assertThrows(Exception403.class, () -> taskService.deleteTask(taskId, user1));
            }
        }

        @Nested
        @DisplayName("성공")
        class Success {
            @Test
            @DisplayName("1: user 본인")
            void success() {
                //given
                long taskId = 1;
                //when
                taskService.deleteTask(taskId, user);
                //then
                verify(taskRepository, times(1)).findById(taskId);
                verify(assignRepository, times(1)).findAssigneeByTaskId(taskId);
                Assertions.assertDoesNotThrow(() -> taskService.deleteTask(taskId, user));
            }

            @Test
            @DisplayName("2: admin")
            void test() {
                //given
                long taskId = 1;
                User admin = User.builder().id(2L).role(User.Role.ADMIN).build();
                //when
                taskService.deleteTask(taskId, admin);
                //then
                verify(taskRepository, times(1)).findById(taskId);
                verify(assignRepository, times(1)).findAssigneeByTaskId(taskId);
                Assertions.assertDoesNotThrow(() -> taskService.deleteTask(taskId, admin));
            }
        }
    }

    @Nested
    @DisplayName("Task 상세보기")
    class Detail {
        @Test
        @DisplayName("실패: Task 없음")
        void test() {
            //given
            long taskId = 2;
            //when then
            Assertions.assertThrows(Exception404.class, () -> taskService.getDetailTask(taskId));
        }

        @Test
        @DisplayName("성공")
        void success() {
            //given
            long taskId = 1;
            //when
            taskService.getDetailTask(taskId);
            //then
            verify(taskRepository, times(1)).findById(taskId);
            verify(assignRepository, times(1)).findAssigneeByTaskId(taskId);
            Assertions.assertDoesNotThrow(() -> taskService.getDetailTask(taskId));
        }
    }

    @Nested
    @DisplayName("Task 최근 생성 7개만 가져오기")
    class LatestTasks {

        @Test
        @DisplayName("성공")
        void success() {
            //when
            taskService.getLatestTasks();

            verify(taskJPQLRepository, times(1)).findLatestTasks();
            verify(assignRepository, times(1)).findAssigneeByTaskId(1L);
            Assertions.assertDoesNotThrow(() -> taskService.getLatestTasks());
        }
    }

    @Nested
    @DisplayName("[Dashboard] Perfomance(최근 2주동안의) data")
    class Perfomance {

        @Test
        @DisplayName("성공")
        void success() {
            //given
            date = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS)
                    .plusDays(1).minusNanos(1).minusWeeks(2);

            for (int i = 0; i < 14; i++) {
                date = date.plusDays(1);

                lenient().when(taskJPQLRepository.findTasksByDate(date))
                        .thenReturn(List.of(task));

                lenient().when(taskJPQLRepository.findDoneCountByDate(date))
                        .thenReturn(1);
            }

            //when
            taskService.getPerfomance();

            //then
            verify(taskJPQLRepository, times(1)).findTasksByDate(date);
            verify(assignRepository, times(14)).findAssignCountByTaskId(1L);
            verify(taskJPQLRepository, times(1)).findDoneCountByDate(date);
            Assertions.assertDoesNotThrow(() -> taskService.getPerfomance());
        }
    }


    @Nested
    @DisplayName("[DashBoard] 최근 1주일간의 통계 데이터")
    class Progress {

        @Test
        @DisplayName("성공")
        void success() {
            //given
            date = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS)
                    .plusDays(1).minusNanos(1).minusWeeks(1);

            for (int i = 0; i < 7; i++) {
                date = date.plusDays(1);

                lenient().when(taskJPQLRepository.findCountByProgressTime(Task.Progress.DONE, date))
                        .thenReturn(1);
                lenient().when(taskJPQLRepository.findCountByProgressTime(Task.Progress.TODO, date))
                        .thenReturn(2);
                lenient().when(taskJPQLRepository.findCountByProgressTime(Task.Progress.IN_PROGRESS, date))
                        .thenReturn(3);

            }

            //when
            taskService.getProgress();

            //then
            verify(taskJPQLRepository, times(1)).findCountByProgressTime(Task.Progress.DONE, date);
            verify(taskJPQLRepository, times(1)).findCountByProgressTime(Task.Progress.TODO, date);
            verify(taskJPQLRepository, times(1)).findCountByProgressTime(Task.Progress.IN_PROGRESS, date);
            Assertions.assertDoesNotThrow(() -> taskService.getProgress());
        }
    }

    @Nested
    @DisplayName("[Kanban] 내가 속한 Task 가져오기")
    class Kanban {

        @Test
        @DisplayName("성공")
        void success() {
            //given
            long userId = 1L;
            long taskId = 1L;

            lenient().when(taskJPQLRepository.findTasksByUserId(userId))
                    .thenReturn(List.of(task));

            //when
            taskService.getKanban(userId);

            //then
            verify(taskJPQLRepository, times(1)).findTasksByUserId(userId);
            verify(assignRepository, times(1)).findAssignTaskByUserId(userId);
            verify(assignRepository, times(1)).findAssigneeByTaskId(taskId);
            Assertions.assertDoesNotThrow(() -> taskService.getKanban(userId));
        }
    }

    @Nested
    @DisplayName("[Calendar] 조회")
    class Calendar {

        @Test
        @DisplayName("성공")
        void success() {
            //given
            long taskId = 1L;
            int year = 2023;
            int month = 4;
            LocalDate lDate = LocalDate.of(year, month, 1);

            lenient().when(taskJPQLRepository.findTaskByStartEndDate(lDate))
                    .thenReturn(List.of(task));

            //when
            taskService.getCalendar(year, month);

            //then
            verify(taskJPQLRepository, times(1)).findTaskByStartEndDate(lDate);
            verify(assignRepository, times(1)).findAssigneeByTaskId(taskId);
            Assertions.assertDoesNotThrow(() -> taskService.getCalendar(year, month));
        }
    }

    @Nested
    @DisplayName("Overview Daily")
    class DailyTasks {

        @Test
        @DisplayName("성공")
        void success() {
            //given
            long taskId = 1L;
            LocalDate lDate = LocalDate.of(2023, 4, 1);
            Pageable pageable = PageRequest.of(0,10);

            lenient().when(taskRepository.findByDate(lDate, pageable))
                    .thenReturn(new PageImpl<>(List.of(task),pageable,1));

            //when
            taskService.getDailyTasks(lDate, pageable);

            //then
            verify(taskRepository, times(1)).findByDate(lDate, pageable);
            verify(assignRepository, times(1)).findAssigneeByTaskId(taskId);
            Assertions.assertDoesNotThrow(() -> taskService.getDailyTasks(lDate, pageable));
        }
    }

    @Nested
    @DisplayName("Overview Period")
    class PickedTasks {

        @Test
        @DisplayName("성공")
        void success() {
            //given
            long taskId = 1L;
            LocalDate startDate = LocalDate.of(2023, 4, 1);
            LocalDate endDate = LocalDate.of(2023, 4, 1);
            Pageable pageable = PageRequest.of(0,10);

            lenient().when(taskRepository.findTasksByBetweenDate(startDate, endDate, pageable))
                    .thenReturn(new PageImpl<>(List.of(task),pageable,1));

            //when
            taskService.getPickedTasks(startDate, endDate, pageable);

            //then
            verify(taskRepository, times(1)).findTasksByBetweenDate(startDate, endDate, pageable);
            verify(assignRepository, times(1)).findAssigneeByTaskId(taskId);
            Assertions.assertDoesNotThrow(() -> taskService.getPickedTasks(startDate, endDate, pageable));
        }
    }

}