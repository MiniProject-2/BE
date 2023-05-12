package com.task.needmoretask.model.profile;

import lombok.*;

import javax.persistence.*;

@Getter @Builder
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Entity @Table(name = "profile_tb")
public class Profile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String url;
}
