package com.task.needmoretask.dto.task;

import com.task.needmoretask.model.task.Task;
import com.task.needmoretask.model.user.User;
import lombok.Builder;
import lombok.Getter;

import javax.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.List;

@Getter @Builder
public class TaskRequest {
    @NotBlank
    private LocalDate startAt;
    @NotBlank
    private LocalDate endAt;
    @NotBlank
    private String title;
    @NotBlank
    private String desc;
    private List<AssigneeRequest> assignee;
    @NotBlank
    private Task.Priority priority;
    @NotBlank
    private Task.Progress progress;
    @Getter
    public static class AssigneeRequest{
        @NotBlank
        private Long userId;
    }

    public Task toEntity(User user){
        return Task.builder()
                .user(user)
                .startAt(startAt)
                .endAt(endAt)
                .title(title)
                .description(desc)
                .progress(progress)
                .priority(priority)
                .status(true)
                .build();
    }
}
