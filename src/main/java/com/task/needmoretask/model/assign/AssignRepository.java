package com.task.needmoretask.model.assign;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AssignRepository extends JpaRepository<Assignment,Long> {

    @Query("select a from Assignment a join fetch a.user join fetch a.task where a.task.id = :taskId")
    Optional<List<Assignment>> findAssigneeByTaskId(Long taskId);
}
