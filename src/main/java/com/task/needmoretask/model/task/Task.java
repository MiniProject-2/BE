package com.task.needmoretask.model.task;

import com.task.needmoretask.model.user.User;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;

@Getter
@NoArgsConstructor
@Entity @Table(name = "task_tb")
public class Task {
    @Id @GeneratedValue
    private Long id;
    @Column(nullable = false)
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
    @Column(nullable = false)
    private Progress progress;
    @Column(nullable = false)
    private Priority priority;
    @Column(nullable = false)
    private boolean status;

    public enum Progress{
        TODO,IN_PROGRESS,DONE
    }
    public enum Priority{
        URGENT,HIGH,MEDIUM,LOW
    }

    @Builder
    public Task(User user, LocalDate startAt, LocalDate endAt, Progress progress, Priority priority) {
        this.user = user;
        this.startAt = startAt;
        this.endAt = endAt;
        this.progress = progress;
        this.priority = priority;
        this.status = true;
    }

    void update(LocalDate startAt, LocalDate endAt, Progress progress, Priority priority){
        this.startAt = startAt;
        this.endAt = endAt;
        this.progress = progress;
        this.priority = priority;
    }

    void deactivateTask(){
        this.status = false;
    }
}
