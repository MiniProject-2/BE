package com.task.needmoretask.controller;

import com.task.needmoretask.core.auth.session.MyUserDetails;
import com.task.needmoretask.dto.ResponseDTO;
import com.task.needmoretask.dto.task.TaskRequest;
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

@RequestMapping("/api")
@RestController
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;

    // Task 생성
    @PostMapping("/task")
    public ResponseEntity<?> createTask(@RequestBody @Valid TaskRequest taskRequest, Errors errors, @AuthenticationPrincipal MyUserDetails myUserDetails){
        taskService.createTask(taskRequest,myUserDetails.getUser());
        return ResponseEntity.ok(new ResponseDTO<>());
    }
}
