package com.task.needmoretask.dto.task;

import com.task.needmoretask.model.assign.Assignment;
import com.task.needmoretask.model.task.Task;
import com.task.needmoretask.model.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
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

    @AllArgsConstructor @Builder @Getter
    public static class Delete{
        private Long taskId;
        private boolean isDeleted;
        private List<AssignmentResponse> assignee;

        @AllArgsConstructor @Builder @Getter
        public static class AssignmentResponse{
            private Long assignId;
            private boolean isDeleted;
        }
    }

    @Getter
    public static class Detail{
        private UserResponse taskOwner;
        private ZonedDateTime createdAt;
        private ZonedDateTime updatedAt;
        private LocalDate startAt;
        private LocalDate endAt;
        private String title;
        private String desc;
        private List<Detail.AssignmentResponse> assignee;
        private Task.Priority priority;
        private Task.Progress progress;

        @Builder
        public Detail(UserResponse taskOwner, List<Detail.AssignmentResponse> assignee, Task task) {
            this.taskOwner = taskOwner;
            this.createdAt = task.getCreatedAt();
            this.updatedAt = task.getUpdatedAt();
            this.startAt = task.getStartAt();
            this.endAt = task.getEndAt();
            this.title = task.getTitle();
            this.desc = task.getDescription();
            this.assignee = assignee;
            this.priority = task.getPriority();
            this.progress = task.getProgress();
        }

        @Getter
        public static class UserResponse{
            private Long userId;
            private String fullName;
            private String profileImageUrl;

            public UserResponse(Task task) {
                this.userId = task.getUser().getId();
                this.fullName = task.getUser().getFullname();
                this.profileImageUrl = task.getUser().getProfile().getUrl();
            }
        }

        @Getter
        public static class AssignmentResponse{
            private Long userId;
            private String profileImageUrl;

            public AssignmentResponse(Assignment assignment) {
                this.userId = assignment.getUser().getId();
                this.profileImageUrl = assignment.getUser().getProfile().getUrl();
            }
        }
    }

    // dashboard 가장 최신 7개 전달 DTO
    @Getter
    public static class LatestTaskOutDTO{
        private Long taskId;

        private TaskOwner taskOwner;

        private ZonedDateTime createdAt;
        private ZonedDateTime updatedAt;
        private LocalDate startAt;
        private LocalDate endAt;
        private String title;
        private List<AssignmentDTO> assignees;

        private Task.Priority priority;
        private Task.Progress progress;

        @Builder
        public LatestTaskOutDTO(Task task, List<Assignment> assignments) {
            this.taskId = task.getId();
            this.taskOwner = new TaskOwner(
                    task.getUser().getId(),
                    task.getUser().getFullname(),
                    task.getUser().getProfile().getUrl(),
                    task.getUser().getDepartment());

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
        public class TaskOwner{
            private Long userId;
            private String fullname;
            private String profileImageUrl;
            private User.Department department;

            public TaskOwner(Long userId, String fullname, String profileImageUrl, User.Department department) {
                this.userId = userId;
                this.fullname = fullname;
                this.profileImageUrl = profileImageUrl;
                this.department = department;
            }
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
        private int taskCount;
        private int doneCount;

        public PerformanceOutDTO(LocalDate date, int assignedTaskCount, int doneCount) {
            this.date = date;
            this.taskCount = assignedTaskCount;
            this.doneCount = doneCount;
        }
    }

    @Getter
    public static class ProgressOutDTO{
        private Total done;
        private Total inProgress;
        private Total todo;

        @Builder
        public ProgressOutDTO(LocalDate[] date, int doneTotalCnt, int[] doneList,
                              int inProgressTotalCnt, int[] inProgressList,
                              int todoTotalCnt, int[] todoList) {
            this.done = new Total(doneTotalCnt, date, doneList);
            this.inProgress = new Total(inProgressTotalCnt, date, inProgressList);
            this.todo = new Total(todoTotalCnt, date, todoList);
        }

        @Getter
        public class Total{
            private int totalCount;
            private List<Graph> graph;

            public Total(int totalCount, LocalDate[] date, int[] cntList) {
                this.totalCount = totalCount;

                this.graph = new ArrayList<>();
                for (int i = 0; i < date.length; i++) {
                    graph.add(new Graph(date[i], cntList[i]));
                }
            }
        }

        @Getter
        public class Graph{
            private LocalDate date;
            private int count;

            public Graph(LocalDate date, int count) {
                this.date = date;
                this.count = count;
            }
        }
    }

    @Getter
    public static class KanbanOutDTO{
        private Long taskId;

        private TaskOwner taskOwner;

        private ZonedDateTime createdAt;
        private ZonedDateTime updatedAt;
        private LocalDate startAt;
        private LocalDate endAt;
        private String title;
        private String desc;
        private List<AssignmentDTO> assignees;

        private Task.Priority priority;
        private Task.Progress progress;

        @Builder
        public KanbanOutDTO(Task task, List<Assignment> assignments) {
            this.taskId = task.getId();
            this.taskOwner = new TaskOwner(
                    task.getUser().getId(),
                    task.getUser().getFullname(),
                    task.getUser().getProfile().getUrl());

            this.createdAt = task.getCreatedAt();
            this.updatedAt = task.getUpdatedAt();
            this.startAt = task.getStartAt();
            this.endAt = task.getEndAt();
            this.title = task.getTitle();
            this.desc = task.getDescription();

            this.assignees = assignments.stream().map(a -> new AssignmentDTO(a.getUser())).collect(Collectors.toList());
            this.priority = task.getPriority();
            this.progress = task.getProgress();
        }

        @Getter
        public class TaskOwner{
            private Long userId;
            private String fullname;
            private String profileImageUrl;

            public TaskOwner(Long userId, String fullname, String profileImageUrl) {
                this.userId = userId;
                this.fullname = fullname;
                this.profileImageUrl = profileImageUrl;
            }
        }
        @Getter
        public class AssignmentDTO{
            private Long userId;
            private String profileImageUrl;

            public AssignmentDTO(User user) {
                this.userId = user.getId();
                this.profileImageUrl = user.getProfile().getUrl();
            }
        }
    }
}
