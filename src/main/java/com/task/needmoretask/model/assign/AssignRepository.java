package com.task.needmoretask.model.assign;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AssignRepository extends JpaRepository<Assignment,Long> {

    @Query("select a from Assignment a join fetch a.user join fetch a.task where a.task.id = :taskId and a.status = true")
    Optional<List<Assignment>> findAssigneeByTaskId(Long taskId);

    // task의 assign된 user 수 가져오기
    @Query("select COUNT(a) from Assignment a where a.task.id = :taskId and a.status = true")
    Optional<Integer> findAssignCountByTaskId(Long taskId);
}
