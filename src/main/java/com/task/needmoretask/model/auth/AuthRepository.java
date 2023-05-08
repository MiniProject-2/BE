package com.task.needmoretask.model.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AuthRepository extends JpaRepository<Auth, String> {
    @Query("select a from Auth a where a.userId=:userId and a.isRevoked=false")
    Optional<Auth> findAuthByUserId(@Param("userId") Long userId);

    @Query("select a from Auth a where a.accessToken=:accessToken and a.isRevoked=false")
    Optional<Auth> findAuthByAccessToken(@Param("accessToken") String accessToken);
}
