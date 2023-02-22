/*
package com.project.stress_traffic_system.facade;

import com.project.stress_traffic_system.order.service.OrderService;
import com.project.stress_traffic_system.product.repository.ProductRedisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedissonLockStockFacade {
    private final RedissonClient redissonClient;
    private final OrderService orderService;

    public void decrease(Long key, int quantity) {
        RLock lock = redissonClient.getLock(key.toString());

        try {
            //얼마만큼이나 lock을 점유하고 있을 것인지
            boolean available = lock.tryLock(5, 1, TimeUnit.SECONDS);
            if (!available) {
                log.info("lock획득 실패");
                return;
            }

            orderService.decrease(key, quantity);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }finally {
            lock.unlock();
        }
    }

}
*/
