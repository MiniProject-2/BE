package com.task.needmoretask.model.task;

import com.task.needmoretask.core.util.Timestamped;
import com.task.needmoretask.dto.task.TaskRequest;
import com.task.needmoretask.model.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Objects;

@Getter @Builder
@NoArgsConstructor
@AllArgsConstructor
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

    public void update(TaskRequest taskRequest){
        this.startAt = taskRequest.getStartAt();
        this.endAt = taskRequest.getEndAt();
        this.progress = taskRequest.getProgress();
        this.priority = taskRequest.getPriority();
    }

    public void deactivateTask(){
        this.status = false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return status == task.status && user.equals(task.user) && title.equals(task.title) && description.equals(task.description) && startAt.equals(task.startAt) && endAt.equals(task.endAt) && progress == task.progress && priority == task.priority;
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, title, description, startAt, endAt, progress, priority, status);
    }
}
