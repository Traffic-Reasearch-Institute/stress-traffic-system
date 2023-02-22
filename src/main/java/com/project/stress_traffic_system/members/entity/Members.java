package com.project.stress_traffic_system.members.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Table(name = "USERS")
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Members implements Serializable{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column
    private String email;

    @Column
    private String nickname;

    @Column
    private String address;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private MembersRoleEnum role;

    public Members(String username, String password, String email, String nickname, MembersRoleEnum role) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.nickname = nickname;
        this.role = role;
    }
}
