/*
package com.project.stress_traffic_system.members;//package com.project.stress_traffic_system.members;

import com.project.stress_traffic_system.cart.repository.CartRepository;
import com.project.stress_traffic_system.jwt.JwtUtil;
import com.project.stress_traffic_system.members.dto.LoginRequestDto;
import com.project.stress_traffic_system.members.dto.MembersResponseMsgDto;
import com.project.stress_traffic_system.members.dto.SignupRequestDto;
import com.project.stress_traffic_system.members.entity.Members;
import com.project.stress_traffic_system.members.repository.MembersRepository;
import com.project.stress_traffic_system.members.service.MembersService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.servlet.http.HttpServletResponse;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

//@ExtendWith(MockitoExtension.class) //Mockito 의 가짜 객체를 사용
@SpringBootTest
public class MembersServiceTest {

    @Autowired //로그인할 때는 실제 DB를 이용
    MembersService memberService;

    @Mock //가짜 객체
    MembersRepository MockMembersRepository;

    //MockMembersService 를 만들 때 주입할 객체들//
    @Mock
    CartRepository MockCartRepository;
    @Mock
    JwtUtil MockJwtUtil;
    @Mock
    PasswordEncoder MockPasswordEncoder;

    @Test
    @DisplayName("회원가입 기능 확인")
    public void signup() throws Exception {
        //given
        String username = "usertest";
        String password = "abcde123?";
        String address = "서울";

        SignupRequestDto signupRequestDto = SignupRequestDto.builder()
                .username(username)
                .password(password)
                .address(address)
                .build();

        //test에서만 사용할 custom 레포지터리를 생성
        Members members = new Members();
        //Mock 레포지터리에 멤버를 저장하면 멤버를 반환한다.
        when(MockMembersRepository.save(any(Members.class))).thenReturn(members);

        //Mock 서블릿 생성
        HttpServletResponse response = mock(HttpServletResponse.class);
        //Mock 서비스 생성
        MembersService MockMembersService = new MembersService(MockMembersRepository,MockCartRepository,MockJwtUtil,MockPasswordEncoder);

        //when 회원가입을 하면 Mock 레포터리에 저장된다.
        MembersResponseMsgDto responseMsgDto = MockMembersService.signup(signupRequestDto,response);

        //then
        assertThat(responseMsgDto.getMsg()).isEqualTo("회원가입 성공");
    }

    @Test
    @DisplayName("로그인 기능 확인")
    public void login() throws Exception {
        //given
        //실제 DB에 있는 아이디와 비밀번호
        String username = "zser27";
        String password = "abcde123?";

        LoginRequestDto loginRequestDto = LoginRequestDto.builder()
                .username(username)
                .password(password)
                .build();

        HttpServletResponse response = mock(HttpServletResponse.class);

        //when
        MembersResponseMsgDto responseMsgDto =  memberService.login(loginRequestDto, response);

        //then
        assertThat(responseMsgDto.getMsg()).isEqualTo("로그인 성공");
    }

}
*/