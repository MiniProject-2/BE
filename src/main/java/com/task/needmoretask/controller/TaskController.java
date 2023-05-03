package com.task.needmoretask.controller;

import com.task.needmoretask.core.auth.session.MyUserDetails;
import com.task.needmoretask.dto.ResponseDTO;
import com.task.needmoretask.dto.task.TaskRequest;
import com.task.needmoretask.dto.task.TaskResponse;
import com.task.needmoretask.model.assign.AssignRepository;
import com.task.needmoretask.model.assign.Assignment;
import com.task.needmoretask.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import com.task.needmoretask.model.task.Task;
import com.task.needmoretask.model.task.TaskJPQLRepository;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;

@RequestMapping("/api")
@RestController
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;

    private TaskJPQLRepository taskJPQLRepository;
    private AssignRepository assignRepository;

    // Task 생성
    @PostMapping("/task")
    public ResponseEntity<?> createTask(@RequestBody @Valid TaskRequest taskRequest, Errors errors, @AuthenticationPrincipal MyUserDetails myUserDetails) {
        taskService.createTask(taskRequest, myUserDetails.getUser());
        return ResponseEntity.ok(new ResponseDTO<>());
    }

    // [DashBoard] 가장 최근 생성된 task 7개 return
    @GetMapping("/tasks/latest")
    public ResponseEntity<?> getLatestTasks(){
        List<TaskResponse.LatestTaskOutDTO> responseList;
        responseList = taskService.getLatestTasks();

        return ResponseEntity.ok().body(new ResponseDTO<>(responseList));
    }
}
