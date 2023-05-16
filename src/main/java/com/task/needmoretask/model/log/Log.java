package com.task.needmoretask.model.log;

import com.task.needmoretask.core.util.Timestamped;
import com.task.needmoretask.model.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Getter @Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "log_tb")
public class Log extends Timestamped {
    @Id @GeneratedValue
    private Long id;
    @OneToOne(fetch = FetchType.LAZY)
    private User user;
    @Column(nullable = false)
    private String userAgent;
    @Column(nullable = false)
    private String clientIP;

    public void LastLoginDate(String userAgent, String clientIP){
        this.userAgent = userAgent;
        this.clientIP = clientIP;
    }
}
