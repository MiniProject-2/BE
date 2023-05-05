package com.task.needmoretask.model.assign;

import com.task.needmoretask.model.task.Task;
import com.task.needmoretask.model.user.User;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Objects;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "assign_tb")
public class Assignment {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;
    @ManyToOne(fetch = FetchType.LAZY,cascade = CascadeType.PERSIST)
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Assignment that = (Assignment) o;
        return user.equals(that.user) && task.equals(that.task);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, task);
    }
}
