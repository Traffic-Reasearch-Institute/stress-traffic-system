/*
package com.project.stress_traffic_system.product.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class ProductRedisRepository {
    private final RedisTemplate<String, String> redisTemplate;

    public Boolean lock(Long key) {
        return  redisTemplate
                .opsForValue()
                .setIfAbsent(generatedKey(key), "lock", Duration.ofMillis(3_000));
    }

    public Boolean unlock(Long key) {
        return redisTemplate.delete(generatedKey(key));
    }

    private String generatedKey(Long key) {
        return key.toString();
    }
}
*/
