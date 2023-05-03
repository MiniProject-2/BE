package com.task.needmoretask.model.task;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task,Long> {

    @Query("select t from Task t join fetch t.user order by t.createdAt desc limit 7")
    Optional<List<Task>> findLatestTasks();
}
