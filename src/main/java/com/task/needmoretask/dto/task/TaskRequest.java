package com.task.needmoretask.dto.task;

import com.task.needmoretask.model.task.Task;
import com.task.needmoretask.model.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

@Getter @Builder
public class TaskRequest {
    @NotNull
    private LocalDate startAt;
    @NotNull
    private LocalDate endAt;
    @NotBlank
    private String title;
    @NotBlank
    private String desc;
    private List<AssigneeRequest> assignee;
    @NotNull
    private Task.Priority priority;

    @AssertTrue(message = "startAt must be before or equals endAt")
    private boolean isStartAtBeforeEndAt(){
        return startAt.isBefore(endAt) || startAt.isEqual(endAt);
    }
    @NotNull
    private Task.Progress progress;

    public boolean isAssigneeEmpty(){
        return assignee.isEmpty();
    }

    @Getter @Builder
    @NoArgsConstructor
    @AllArgsConstructor
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
                .build();
    }
}
