package com.task.needmoretask.model.task;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@RequiredArgsConstructor
@Repository
public class TaskJPQLRepository {
    private final EntityManager em;

    public List<Task> findLatestTasks(){
        List<Task> taskListPS =
                em.createQuery("select t from Task t join fetch t.user order by t.createdAt desc limit 7")
                .getResultList();

        return taskListPS;
    }
}
