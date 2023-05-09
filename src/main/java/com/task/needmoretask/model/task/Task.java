package com.task.needmoretask.model.task;

import com.task.needmoretask.core.util.Timestamped;
import com.task.needmoretask.dto.task.TaskRequest;
import com.task.needmoretask.model.user.User;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Objects;

@Getter
@NoArgsConstructor
@Entity @Table(name = "task_tb")
public class Task extends Timestamped {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @Column(nullable = false, length = 20)
    private String title;
    @Lob
    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private LocalDate startAt;
    @Column(nullable = false)
    private LocalDate endAt;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Progress progress;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority;
    @Column(nullable = false)
    private boolean isDeleted;

    public enum Progress{
        TODO,IN_PROGRESS,DONE
    }
    public enum Priority{
        URGENT,HIGH,MEDIUM,LOW
    }

    @Builder
    public Task(Long id, User user, String title, String description, LocalDate startAt, LocalDate endAt, Progress progress, Priority priority) {
        this.id = id;
        this.user = user;
        this.title = title;
        this.description = description;
        this.startAt = startAt;
        this.endAt = endAt;
        this.progress = progress;
        this.priority = priority;
        this.isDeleted = false;
    }

    public void update(TaskRequest taskRequest){
        this.title = taskRequest.getTitle();
        this.description = taskRequest.getDesc();
        this.startAt = taskRequest.getStartAt();
        this.endAt = taskRequest.getEndAt();
        this.progress = taskRequest.getProgress();
        this.priority = taskRequest.getPriority();
    }

    public void deactivateTask(){
        this.isDeleted = true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return isDeleted == task.isDeleted && user.equals(task.user) && title.equals(task.title) && description.equals(task.description) && startAt.equals(task.startAt) && endAt.equals(task.endAt) && progress == task.progress && priority == task.priority;
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, title, description, startAt, endAt, progress, priority, isDeleted);
    }
}
