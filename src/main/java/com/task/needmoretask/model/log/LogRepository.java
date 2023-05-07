package com.task.needmoretask.model.log;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface LogRepository extends JpaRepository<Log, Long> {
    @Query("select l from Log l where l.user.email=:email and l.user.isDeleted=false")
    Optional<Log> findLogByEmail(@Param("email") String email);
}
