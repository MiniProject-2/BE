package com.task.needmoretask.model.task;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@RequiredArgsConstructor
@Repository
public class TaskJPQLRepository {

    private final EntityManager em;

    // 가장 최근 생성한 task 7개 가져오기
    public List<Task> findLatestTasks(){
       TypedQuery<Task> query =
                em.createQuery("select t " +
                                "from Task t " +
                                "join fetch t.user " +
                                "where t.isDeleted = false " +
                                "order by t.createdAt desc"
                ,Task.class);

       query.setFirstResult(0);
       query.setMaxResults(7);


        List<Task> taskListPS = query.getResultList();

        return taskListPS;
    }
    // date 이전에 존재했던 Task 가져오기
    public List<Task> findTasksByDate(ZonedDateTime date){
        TypedQuery<Task> query =
                em.createQuery("select t " +
                                        "from Task t " +
                                        "where (t.updatedAt >= :date " +
                                        "and t.isDeleted = true) " +
                                        "or (t.createdAt <= :date " +
                                        "and t.isDeleted = false)"
                                , Task.class)
                        .setParameter("date", date);


//        Long tastCnt =
//                em.createQuery("select COUNT(t) " +
//                        "from Task t " +
//                        "where t.createdAt <= :date " +
//                        "and t.endAt >= :date", Long.class)
//                        .setParameter("date", date)
//                        .getSingleResult();


        return query.getResultList();
    }

    // date 이전에 존재했던 DONE 인 Task수 select
    public int findDoneCountByDate(ZonedDateTime date){
        Long tastCnt =
                em.createQuery("select COUNT(t) " +
                                        "from Task t " +
                                        "where ((t.updatedAt >= :date " +
                                        "and t.isDeleted = true) " +
                                        "or (t.createdAt <= :date " +
                                        "and t.isDeleted = false))" +
                                        "and t.progress = :done"
                                , Long.class)
                        .setParameter("date", date)
                        .setParameter("done", Task.Progress.DONE)
                        .getSingleResult();

        return  tastCnt.intValue();
    }

    // date날짜에 progressType된 Task수 select
    public int findCountByProgressTime(Task.Progress progress, ZonedDateTime date){
        Long taskDoneCnt =
                em.createQuery("select COUNT(t) " +
                                        "from Task t " +
                                        "where t.progress = :progress " +
                                        "and t.updatedAt >= :startDate " +
                                        "and t.updatedAt < :endDate " +
                                        "and t.isDeleted = false"
                                , Long.class)
                        .setParameter("progress", progress)
                        .setParameter("startDate", date.toLocalDate().atStartOfDay(ZoneId.systemDefault()))
                        .setParameter("endDate", date.toLocalDate().plusDays(1).atStartOfDay(ZoneId.systemDefault()))
                        .getSingleResult();

        return taskDoneCnt.intValue();
    }

    // OwnerId가 userId랑 일치하는 Task 검색
    public List<Task> findTasksByUserId(Long userId){
        TypedQuery<Task> query =
                em.createQuery("select t " +
                                "from Task t " +
                                "where t.isDeleted = false " +
                                "and t.user.id = :userId"
                        ,Task.class)
                                .setParameter("userId", userId);

        return query.getResultList();
    }

    public List<Task> findTaskByStartEndDate(LocalDate date){
        TypedQuery<Task> query =
                em.createQuery("select t " +
                        "from Task t " +
                        "where t.isDeleted = false " +
                        "and ((YEAR(t.startAt) = YEAR(:date) " +
                                        "and MONTH(t.startAt) = MONTH(:date)) " +
                        "or (YEAR(t.endAt) = YEAR(:date) " +
                                        "and MONTH(t.endAt) = MONTH(:date)))"
                , Task.class)
                        .setParameter("date", date);

        return query.getResultList();
    }

}
