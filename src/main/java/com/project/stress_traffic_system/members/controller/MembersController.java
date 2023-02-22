package com.project.stress_traffic_system.members.controller;

import com.project.stress_traffic_system.members.dto.LoginRequestDto;
import com.project.stress_traffic_system.members.dto.MembersCheckRequestMsgDto;
import com.project.stress_traffic_system.members.dto.SignupRequestDto;
import com.project.stress_traffic_system.members.dto.MembersResponseMsgDto;
import com.project.stress_traffic_system.members.service.MembersService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@Slf4j
@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MembersController {
    private final MembersService membersService;

    @PostMapping("/signup")
    public MembersResponseMsgDto signup(@RequestBody SignupRequestDto membersRequestDto, HttpServletResponse response) {
        return membersService.signup(membersRequestDto, response);
    }

    @PostMapping("/login")
    public MembersResponseMsgDto login(@RequestBody LoginRequestDto membersRequestDto, HttpServletResponse response) {
        return membersService.login(membersRequestDto, response);
    }

    @PostMapping("/checkId")
    public ResponseEntity<String> checkId(@RequestBody MembersCheckRequestMsgDto membersCheckRequestMsgDto, HttpServletResponse response) {
        log.info("Members Username exists check");
        boolean existsId = membersService.checkId(membersCheckRequestMsgDto, response);
        if (existsId == true)
            return new ResponseEntity<>("no", HttpStatus.NO_CONTENT);
        else
            return new ResponseEntity<>("yes", HttpStatus.OK);

    }

    @PostMapping("/logTest")
    public void logTest() {
        int test = 0/0;
        log.trace("trace Log");
        log.debug("Debug Log");
        log.info("info Log");
        log.warn("Warn Log");
        log.error("Error Log");
    }
}
