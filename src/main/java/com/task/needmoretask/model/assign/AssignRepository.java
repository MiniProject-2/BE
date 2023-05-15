package com.task.needmoretask.model.assign;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AssignRepository extends JpaRepository<Assignment,Long> {

    @Query("select a from Assignment a join fetch a.user u join fetch u.profile p join fetch a.task t where t.id = :taskId and a.isDeleted = false")
    Optional<List<Assignment>> findAssigneeByTaskId(@Param("taskId") Long taskId);

    // task의 assign된 user 수 가져오기
    @Query("select COUNT(a) from Assignment a where a.task.id = :taskId and a.isDeleted = false")
    Optional<Integer> findAssignCountByTaskId(@Param("taskId") Long taskId);

    @Query("select a from Assignment a join fetch a.task t " +
            "where a.isDeleted = false " +
            "and a.user.id = :userId")
    Optional<List<Assignment>> findAssignTaskByUserId(@Param("userId") Long userId);
}
