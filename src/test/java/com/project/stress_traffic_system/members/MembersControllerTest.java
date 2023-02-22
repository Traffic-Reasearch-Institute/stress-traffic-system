/*
package com.project.stress_traffic_system.members;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.stress_traffic_system.members.dto.LoginRequestDto;
import com.project.stress_traffic_system.members.dto.SignupRequestDto;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class MembersControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @DisplayName("회원가입 테스트")
    public void signup() throws Exception{
        //given
        String username = RandomStringUtils.randomAlphabetic(10);
        String password = "abcde123?";
        String address = "서울";

        SignupRequestDto signupRequestDto = SignupRequestDto.builder()
                .username(username)
                .password(password)
                .address(address)
                .build();

        // when & then
        mockMvc.perform(post("/api/members/signup")
                        .content(objectMapper.writeValueAsString(signupRequestDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("로그인 테스트")
    public void login() throws Exception {
        //given
        //실제 저장 되어 있는 아이디
        String username = "zser27";
        String password = "abcde123?";

        LoginRequestDto loginRequestDto = LoginRequestDto.builder()
                .username(username)
                .password(password)
                .build();

        // when & then
        mockMvc.perform(post("/api/members/login")
                        .content(objectMapper.writeValueAsString(loginRequestDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

}
*/
