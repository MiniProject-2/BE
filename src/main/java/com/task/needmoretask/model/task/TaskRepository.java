package com.task.needmoretask.model.task;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task,Long> {
    @Query("select t from Task t join fetch t.user u where t.id=:id and t.status=true")
    Optional<Task> findById(Long id);

}
