package com.task.needmoretask.service;

import com.task.needmoretask.dto.task.TaskRequest;
import com.task.needmoretask.model.task.TaskRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
class TaskServiceTest {

    @InjectMocks
    private TaskService taskService;

    @Mock
    private TaskRepository taskRepository;

    @Nested
    @DisplayName("Task 작성")
    class Create{
        @Nested
        @DisplayName("작성 실패")
        class Fail{

        }
        @Test
        @DisplayName("작성 성공")
        void success() {
            //given
            TaskRequest request = TaskRequest.builder().build();
            //when
            //then
        }
    }
}