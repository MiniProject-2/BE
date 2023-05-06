package com.task.needmoretask.service;


import com.task.needmoretask.core.exception.Exception403;
import com.task.needmoretask.core.exception.Exception404;
import com.task.needmoretask.core.exception.Exception500;
import com.task.needmoretask.dto.task.TaskRequest;
import com.task.needmoretask.dto.task.TaskResponse;
import com.task.needmoretask.model.assign.AssignRepository;
import com.task.needmoretask.model.assign.Assignment;
import com.task.needmoretask.model.task.Task;
import com.task.needmoretask.model.task.TaskJPQLRepository;
import com.task.needmoretask.model.task.TaskRepository;
import com.task.needmoretask.model.user.User;
import com.task.needmoretask.model.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskService {
    private final TaskJPQLRepository taskJPQLRepository;
    private final TaskRepository taskRepository;
    private final AssignRepository assignRepository;
    private final UserRepository userRepository;

    // Task 생성
    @Transactional
    public void createTask(TaskRequest request, User user) {
        if (!request.getAssignee().isEmpty()) {
            List<Assignment> assigns = request.getAssignee().stream()
                    .map(assigneeRequest -> {
                        User assignee = userRepository.findById(assigneeRequest.getUserId())
                                .orElseThrow(() -> new Exception404("유저를 찾을 수 없습니다"));
                        return Assignment.builder()
                                .user(assignee)
                                .task(request.toEntity(user))
                                .build();
                    })
                    .collect(Collectors.toList());
            try {
                assignRepository.saveAll(assigns);
            } catch (Exception e) {
                throw new Exception500("Task 생성 실패: " + e.getMessage());
            }
            return;
        }
        try {
            taskRepository.save(request.toEntity(user));
        } catch (Exception e) {
            throw new Exception500("Task 생성 실패: " + e.getMessage());
        }
    }

    @Transactional
    public TaskResponse.Test updateTask(Long id, TaskRequest request, User user) {
        List<Assignment> newAssigns = new ArrayList<>();
        Task task = notFoundTask(id);
        unAuthorizedTask(task, user);
        task.update(request);
        /*
         * Assignee가 있는지 없는지 있다가 없앴는지 없다가 만들었는지 알 수 없기 때문에
         * 일단 해당 Task에 Assignement가 존재했다면 싹 다 지우고
         * request로 assignee를 새로 추가했다면 다시 저장
         * */

        // 해당 Task에 Assignment가 있다면 싹 다 지움
        try {
            assignRepository.findAssigneeByTaskId(id).ifPresent(assignRepository::deleteAll);
        } catch (Exception e) {
            throw new Exception500("Assignment 삭제 실패: " + e.getMessage());
        }

        // request로 Assignee를 등록했다면 새로 저장
        if (!request.getAssignee().isEmpty()) {
            newAssigns = request.getAssignee().stream()
                    .map(assigneeRequest -> {
                        User assignee = userRepository.findById(assigneeRequest.getUserId())
                                .orElseThrow(() -> new Exception404("유저를 찾을 수 없습니다"));
                        return Assignment.builder()
                                .user(assignee)
                                .task(request.toEntity(user))
                                .build();
                    })
                    .collect(Collectors.toList());
            try {
                assignRepository.saveAll(newAssigns);
            } catch (Exception e) {
                throw new Exception500("Task 수정 실패: " + e.getMessage());
            }
        }
        List<TaskResponse.Test.AssignResponse> assignResponses = newAssigns.stream()
                .map(TaskResponse.Test.AssignResponse::new)
                .collect(Collectors.toList());
        return new TaskResponse.Test(task, assignResponses);
    }

    // Task 삭제
    @Transactional
    public TaskResponse.Delete deleteTask(Long id, User user) {
        Task task = notFoundTask(id);
        unAuthorizedTask(task,user);
        List<Assignment> assignments;
        try {
            assignments = assignRepository.findAssigneeByTaskId(task.getId()).orElse(Collections.emptyList());
            assignments.forEach(Assignment::deactivateAssign);
            task.deactivateTask();
        }catch (Exception e){
            throw new Exception500("Task 삭제 실패: " + e.getMessage());
        }
        List<TaskResponse.Delete.AssignmentResponse> assignmentResponses = assignments.stream()
                .map(assignment -> TaskResponse.Delete.AssignmentResponse.builder()
                        .assignId(assignment.getId())
                        .isDeleted(assignment.isDeleted())
                        .build())
                .collect(Collectors.toList());
        return TaskResponse.Delete.builder().taskId(id).isDeleted(task.isDeleted()).assignee(assignmentResponses).build();
    }

    // Task 상세보기
    public TaskResponse.Detail getDetailTask(Long id){
        Task task = notFoundTask(id);
        List<TaskResponse.Detail.AssignmentResponse> assignee = assignRepository.findAssigneeByTaskId(task.getId())
                .map(assignments -> assignments.stream()
                        .map(TaskResponse.Detail.AssignmentResponse::new)
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());

        return TaskResponse.Detail.builder()
                .taskOwner(new TaskResponse.Detail.UserResponse(task))
                .assignee(assignee)
                .task(task)
                .build();
    }

    // [Dashboard] 가장 최근 생성된 task 7개 return
    public List<TaskResponse.LatestTaskOutDTO> getLatestTasks() {
        List<TaskResponse.LatestTaskOutDTO> responseList = new ArrayList<>();
        List<Task> tasksPS = taskJPQLRepository.findLatestTasks();

        List<Assignment> assigneesPS;
        for (int i = 0; i < tasksPS.size(); i++) {
            assigneesPS = assignRepository.findAssigneeByTaskId(tasksPS.get(i).getId()).orElse(new ArrayList<>());

            TaskResponse.LatestTaskOutDTO latestTaskOutDTO = new TaskResponse.LatestTaskOutDTO(
                    tasksPS.get(i), assigneesPS
            );
            responseList.add(latestTaskOutDTO);
        }
        return responseList;
    }
    
    // [Dashboard] Perfomance(최근 2주동안의) data return
    public List<TaskResponse.PerformanceOutDTO> getPerfomance(){
        List<TaskResponse.PerformanceOutDTO> performanceOutDTOList = new ArrayList<>();

        ZonedDateTime date = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS)
                .plusDays(1).minusNanos(1).minusWeeks(2);

        for (int i = 0; i < 14; i++) {
            date = date.plusDays(1);
            List<Task> tasksPS = taskJPQLRepository.findTasksByDate(date);

            System.out.println(tasksPS.size());

            int assignNullSize = 0;
            for (int j = 0; j < tasksPS.size(); j++) {

                int assignSize = assignRepository.findAssignCountByTaskId(tasksPS.get(j).getId())
                        .orElse(0);

                if(assignSize == 0)
                    assignNullSize++;
            }

            int taskCnt = tasksPS.size() - assignNullSize;
            System.out.println(taskCnt);
            int doneCnt = taskJPQLRepository.findDoneCountByDate(date);
            System.out.println(doneCnt);

            performanceOutDTOList.add(new TaskResponse.PerformanceOutDTO(date.toLocalDate(), taskCnt, doneCnt));
        }

        return performanceOutDTOList;
    }

    // [DashBoard] 최근 1주일간의 통계 데이터

    public TaskResponse.ProgressOutDTO getProgress(){
        TaskResponse.ProgressOutDTO progressOutDTO;

        int doneTotalCnt = 0;
        int inProgressTotalCnt = 0;
        int todoTotalCnt = 0;

        int days = 7;
        LocalDate[] progressDate = new LocalDate[days];

        int[] doneCntList = new int[days];
        int[] inProgressCntList = new int[days];
        int[] todoCntList = new int[days];

        ZonedDateTime date = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS)
                .plusDays(1).minusNanos(1).minusWeeks(1);

        for (int i = 0; i < days; i++) {
            date = date.plusDays(1);

            int doneDateCnt = taskJPQLRepository.findCountByProgressTime(Task.Progress.DONE, date);
            int inProgressDateCnt = taskJPQLRepository.findCountByProgressTime(Task.Progress.IN_PROGRESS, date);
            int todoDateCnt = taskJPQLRepository.findCountByProgressTime(Task.Progress.TODO, date);

            doneTotalCnt += doneDateCnt;
            inProgressTotalCnt += inProgressDateCnt;
            todoTotalCnt += todoDateCnt;

            progressDate[i] = date.toLocalDate();
            doneCntList[i] = doneDateCnt;
            inProgressCntList[i] = inProgressDateCnt;
            todoCntList[i] = todoDateCnt;

        }

        progressOutDTO = new TaskResponse.ProgressOutDTO( progressDate,
                doneTotalCnt, doneCntList,
                inProgressTotalCnt, inProgressCntList,
                todoTotalCnt, todoCntList
        );

        return progressOutDTO;
    }

    private Task notFoundTask(Long taskId) {
        return taskRepository.findById(taskId).orElseThrow(
                () -> new Exception404("Task를 찾을 수 없습니다"));
    }

    // Task에 대한 유저 권한 체크(본인 Task가 아닌 경우(어드민이 아닌 유저) 수정, 삭제 불가)
    private void unAuthorizedTask(Task task, User loginUser) {
        if (loginUser.getRole() == User.Role.USER && !task.getUser().getId().equals(loginUser.getId()))
            throw new Exception403("권한이 없습니다");
    }
}
