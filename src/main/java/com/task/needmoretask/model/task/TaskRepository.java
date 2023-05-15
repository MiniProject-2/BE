package com.task.needmoretask.model.task;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task,Long> {
    @Query("select t from Task t join fetch t.user u join fetch u.profile p where t.id=:id and t.isDeleted=false")
    Optional<Task> findById(@Param("id") Long id);

    @Query(value = "select t " +
            "from Task t " +
            "join fetch t.user " +
            "where t.isDeleted = false " +
            "and (t.startAt <= :date " +
            "and t.endAt >= :date) " +
            "order by t.title desc", countQuery = "select COUNT(t) from Task t where t.isDeleted = false")
    Page<Task> findByDate(@Param("date") LocalDate date, Pageable pageable);

    @Query(value = "select t " +
            "from Task t " +
            "join fetch t.user " +
            "where t.isDeleted = false " +
            "and ((t.startAt >= :startDate " +
            "and t.startAt <= :endDate) " +
            "or (t.endAt >= :startDate " +
            "and t.endAt <= :endDate) " +
            "or (:startDate >= t.startAt " +
            "and :endDate <= t.endAt) " +
            "or (t.startAt >= :startDate " +
            "and t.endAt <= :endDate)" +
            ")", countQuery = "select COUNT(t) from Task t where t.isDeleted = false")
    Page<Task> findTasksByBetweenDate(@Param("startDate") LocalDate startDate,
                                      @Param("endDate") LocalDate endDate,
                                      Pageable pageable);
}
