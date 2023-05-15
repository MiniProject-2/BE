package com.task.needmoretask.model.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {
    @Query("select u from User u where u.email=:email and u.isDeleted=false")
    Optional<User> findUserByEmail(@Param("email") String email);

    @Query("select u from User u where u.id=:id and u.isDeleted=false")
    Optional<User> findById(@Param("id") Long id);

    @Query("select u from User u join fetch u.profile p where u.id=:id and u.isDeleted=false")
    Optional<User> findByIdWithProfile(@Param("id") Long id);

    @Query("select u from User u join fetch u.profile p where u.isDeleted=false")
    List<User> findAll();

    @Query(value = "select u from User u join fetch u.profile p where u.isDeleted=false", countQuery = "select count(u) from User u where u.isDeleted=false")
    Page<User> findAll(Pageable pageable);

    @Query(value = "select u from User u join fetch u.profile p where u.role=:role and u.isDeleted=false", countQuery = "select count(u) from User u where u.role=:role and u.isDeleted=false")
    Page<User> findAllByRole(@Param("role") User.Role role, Pageable pageable);

    @Query(value = "select u from User u join fetch u.profile p where u.isDeleted=false and u.fullname like %:fullName%", countQuery = "select count(u) from User u where u.isDeleted=false and u.fullname like %:fullName%")
    Page<User> findUsersByFullName(@Param("fullName") String fullName, Pageable pageable);
}
