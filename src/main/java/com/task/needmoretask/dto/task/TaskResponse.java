package com.task.needmoretask.dto.task;

import com.task.needmoretask.model.task.Task;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class TaskResponse {

    public static class LatestTaskOutDTO{
        private Long taskId;

        private Long ownerUserId;
        private String ownerFullname;
        private String ownerProfile;
        private String department;

        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private LocalDate startAt;
        private LocalDate endAt;
        private String title;
        private ArrayList<AssigneeDTO> assignee;

        Task.Priority priority;
        Task.Progress progress;

        public class AssigneeDTO{
            Long userId;
            String profile;

            public AssigneeDTO(Long userId, String profile) {
                this.userId = userId;
                this.profile = profile;
            }
        }
    }
}
