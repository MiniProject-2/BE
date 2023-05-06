package com.task.needmoretask.model.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {
    @Query("select u from User u where u.email=:email and u.isDeleted=false")
    Optional<User> findUserByEmail(@Param("email") String email);

    @Query("select u from User u where u.id=:id and u.isDeleted=false")
    Optional<User> findById(@Param("id") Long id);

    @Query("select u from User u where u.isDeleted=false")
    Page<User> findAll(Pageable pageable);

    @Query("select u from User u where u.isDeleted=false and u.fullname like %:fullName%")
    Page<User> findUsersByFullName(@Param("fullName") String fullName, Pageable pageable);
}
