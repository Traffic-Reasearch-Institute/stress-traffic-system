/*
package com.project.stress_traffic_system.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import redis.embedded.RedisServer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;

//embedded redis는 profile이 local일 때에만 작동
//application.properties에서 spring.profiles.active=local로 설정해 두었음
@Slf4j
@Profile("local")
@Configuration
public class EmbeddedRedisConfig {

    //application.properties에서 포트 값 가져와서 redisPort에 저장
    @Value("${spring.redis.port}")
    private int redisPort;

    private RedisServer redisServer;

    //객체가 생성될 때 실행됨
    @PostConstruct
    public void redisServer() throws IOException {
        redisServer = RedisServer.builder()
                .port(redisPort)
                .setting("maxmemory 128M")
                .build();
        redisServer.start();
    }

    //객체가 삭제될 때 실행됨
    @PreDestroy
    public void stopRedis() {
        if (redisServer != null) {
            redisServer.stop();
        }
    }

}
*/
