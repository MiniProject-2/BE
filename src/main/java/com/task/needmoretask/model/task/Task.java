package com.task.needmoretask.model.task;

import com.task.needmoretask.core.util.Timestamped;
import com.task.needmoretask.dto.task.TaskRequest;
import com.task.needmoretask.model.user.User;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;

@Getter
@NoArgsConstructor
@Entity @Table(name = "task_tb")
@EqualsAndHashCode(callSuper = false)
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

    public boolean isUserEqual(Long userId){
        return this.user.getId().equals(userId);
    }
}
