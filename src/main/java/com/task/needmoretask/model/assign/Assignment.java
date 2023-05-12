package com.task.needmoretask.model.assign;

import com.task.needmoretask.model.task.Task;
import com.task.needmoretask.model.user.User;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity @Getter
@EqualsAndHashCode
@NoArgsConstructor
@Table(name = "assign_tb")
public class Assignment {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;
    @ManyToOne(fetch = FetchType.LAZY)
    private Task task;
    @Column(nullable = false)
    private boolean isDeleted;

    @Builder
    public Assignment(User user, Task task) {
        this.user = user;
        this.task = task;
        this.isDeleted = false;
    }

    public void deactivateAssign(){
        this.isDeleted = true;
    }
}
