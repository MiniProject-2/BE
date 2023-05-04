package com.task.needmoretask.model.task;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@Repository
public class TaskJPQLRepository {

    private final EntityManager em;

    public List<Task> findLatestTasks(){
       TypedQuery<Task> query =
                em.createQuery("select t " +
                                "from Task t " +
                                "join fetch t.user " +
                                "where t.status = true " +
                                "order by t.createdAt desc"
                ,Task.class);

       query.setFirstResult(0);
       query.setMaxResults(7);


        List<Task> taskListPS = query.getResultList();

        return taskListPS;
    }

}
