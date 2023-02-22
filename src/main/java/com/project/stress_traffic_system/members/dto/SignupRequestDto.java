package com.project.stress_traffic_system.members.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupRequestDto {
    private String username;
    private String password;
    private String email;
    private String nickname;
    private String address;

    @Builder
    public SignupRequestDto(String username, String password, String address, String email, String nickname) {
        this.username = username;
        this.password = password;
        this.address = address;
        this.email = email;
        this.nickname = nickname;
    }
}
