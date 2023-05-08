package com.task.needmoretask.repository;

import com.task.needmoretask.model.assign.AssignRepository;
import com.task.needmoretask.model.assign.Assignment;
import com.task.needmoretask.model.profile.Profile;
import com.task.needmoretask.model.profile.ProfileRepository;
import com.task.needmoretask.model.task.Task;
import com.task.needmoretask.model.task.TaskRepository;
import com.task.needmoretask.model.user.User;
import com.task.needmoretask.model.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("dev")
@Import({BCryptPasswordEncoder.class})
@DataJpaTest
public class AssignRepositoryTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private AssignRepository assignRepository;

    @Autowired
    private EntityManager em;

    long userId1,userId2,taskId;
    @BeforeEach
    void setUp(
            @Autowired ProfileRepository profileRepository
    ) {
        Profile profile = profileRepository.save(new Profile(null, "img.jpg"));
        User user1 = User.builder()
                .email("user1@email.com")
                .password("1234")
                .phone("010-0000-0000")
                .fullname("user1")
                .department(User.Department.HR)
                .joinCompanyYear(2023)
                .profile(profile)
                .role(User.Role.USER)
                .build();
        User user2 = User.builder()
                .email("user2@email.com")
                .password("1234")
                .phone("010-0000-0000")
                .fullname("user2")
                .department(User.Department.DEVELOPMENT)
                .joinCompanyYear(2024)
                .profile(profile)
                .role(User.Role.USER)
                .build();
        List<User> users = List.of(user1,user2);
        userRepository.saveAll(users);
        userId1 = user1.getId();
        userId2 = user2.getId();

        // given
        long userId = 1;
        User userPS = userRepository.findById(userId).orElse(null);
        LocalDate startAt = LocalDate.of(2023, 5, 3);
        LocalDate endAt = LocalDate.of(2023, 6, 3);

        for (int i = 0; i < 8; i++) {
            Task task1 = Task.builder()
                    .user(userPS)
                    .title("title"+i)
                    .description("desc"+i)
                    .startAt(startAt)
                    .endAt(endAt)
                    .progress(Task.Progress.DONE)
                    .priority(Task.Priority.LOW)
                    .build();

            Assignment assignment = Assignment.builder()
                    .user(userPS)
                    .task(task1)
                    .build();

            assignRepository.save(assignment);
        }

    }

    @Test
    @DisplayName("[Kanban] userId로 Assignment 조회")
    @DirtiesContext
    public void findAssignTaskByUserId(){
        Long userId = 1L;
        List<Assignment> assignmentsPS = assignRepository.findAssignTaskByUserId(userId)
                .orElse(new ArrayList<>());

        for(Assignment a:assignmentsPS) {
            System.out.println(a.getId());
            System.out.println(a.getUser());
            System.out.println(a.getTask());
            System.out.println();
        }
        assertThat(assignmentsPS.size()).isEqualTo(8);
    }

}
