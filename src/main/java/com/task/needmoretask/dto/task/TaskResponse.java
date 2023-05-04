package com.task.needmoretask.dto.task;

import com.task.needmoretask.model.assign.Assignment;
import com.task.needmoretask.model.task.Task;
import com.task.needmoretask.model.user.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class TaskResponse {

    @Getter
    public static class Test{
        private Long id;
        private Long taskOwner;
        private String title;
        private String description;
        private LocalDate startAt;
        private LocalDate endAt;
        private Task.Progress progress;
        private Task.Priority priority;
        private List<AssignResponse> assignee;

        public Test(Task task, List<AssignResponse> assignee) {
            this.id = task.getId();
            this.taskOwner = task.getUser().getId();
            this.title = task.getTitle();
            this.description = task.getDescription();
            this.startAt = task.getStartAt();
            this.endAt = task.getEndAt();
            this.progress = task.getProgress();
            this.priority = task.getPriority();
            this.assignee = assignee;
        }

        @Getter
        public static class AssignResponse{
            private Long assignee;

            public AssignResponse(Assignment assignment) {
                this.assignee = assignment.getUser().getId();
            }
        }
    }

    // dashboard 가장 최신 7개 전달 DTO
    @Getter
    public static class LatestTaskOutDTO{
        private Long taskId;

        private Long ownerUserId;
        private String ownerFullname;
        private String ownerProfile;
        private User.Department department;

        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private LocalDate startAt;
        private LocalDate endAt;
        private String title;
        private List<AssignmentDTO> assignees;

        private Task.Priority priority;
        private Task.Progress progress;

        @Builder
        public LatestTaskOutDTO(Task task, List<Assignment> assignments) {
            this.taskId = task.getId();
            this.ownerUserId = task.getUser().getId();
            this.ownerFullname = task.getUser().getFullname();
            this.ownerProfile = task.getUser().getProfile().getUrl();
            this.department = task.getUser().getDepartment();
            this.createdAt = task.getCreatedAt();
            this.updatedAt = task.getUpdatedAt();
            this.startAt = task.getStartAt();
            this.endAt = task.getEndAt();
            this.title = task.getTitle();

            this.assignees = assignments.stream().map(a -> new AssignmentDTO(a.getUser())).collect(Collectors.toList());
            this.priority = task.getPriority();
            this.progress = task.getProgress();
        }

        @Getter
        public class AssignmentDTO{
            private Long userId;
            private String profile;

            public AssignmentDTO(User user) {
                this.userId = user.getId();
                this.profile = user.getProfile().getUrl();
            }
        }
    }

    @Getter
    public static class PerformanceOutDTO{
        private LocalDate date;
        private int assignedTaskCount;
        private int doneCount;

        public PerformanceOutDTO(LocalDate date, int assignedTaskCount, int doneCount) {
            this.date = date;
            this.assignedTaskCount = assignedTaskCount;
            this.doneCount = doneCount;
        }
    }
}
