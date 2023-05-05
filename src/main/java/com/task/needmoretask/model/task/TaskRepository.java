package com.task.needmoretask.model.task;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task,Long> {
    @Query("select t from Task t join fetch t.user u where t.id=:id and t.isDeleted=false")
    Optional<Task> findById(@Param("id") Long id);

}
