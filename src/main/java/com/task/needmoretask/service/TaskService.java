package com.task.needmoretask.service;


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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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
    public void createTask(TaskRequest request, User user){
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
        try{
//            taskRepository.save(request.toEntity(user));
            assignRepository.saveAll(assigns);
        }catch (Exception e){
            throw new Exception500("Task 생성 실패: "+e.getMessage());
        }
    }

    // [Dashboard] 가장 최근 생성된 task 7개 return
    public List<TaskResponse.LatestTaskOutDTO> getLatestTasks(){
        List<Task> tasksPS = taskJPQLRepository.findLatestTasks();

        List<Assignment> assigneesPS;
        List<TaskResponse.LatestTaskOutDTO> responseList = new ArrayList<>();
        for (int i = 0; i < tasksPS.size(); i++) {

            assigneesPS = assignRepository.findAssigneeByTaskId(tasksPS.get(i).getId()).orElseThrow();
            TaskResponse.LatestTaskOutDTO latestTaskOutDTO = new TaskResponse.LatestTaskOutDTO(
                    tasksPS.get(i), assigneesPS
            );
            responseList.add(latestTaskOutDTO);
        }

        return responseList;
    }
}
