package com.project.stress_traffic_system.members.repository;

import com.project.stress_traffic_system.members.entity.Members;
import com.project.stress_traffic_system.redis.CacheKey;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MembersRepository extends JpaRepository<Members, Long> {

    //p0은 해당되는 함수의 첫번째 인자. 그러므로 여기서는 username이 키 값으로 사용됨
    //value -> 캐시의 이름을 설정하는 속성. cacheNames로도 사용할 수 있음.
    //key -> 캐시의 키를 정함.
//    @Cacheable(value = CacheKey.USERNAME, key = "#p0")
    Optional<Members> findByUsername(String username);

    Optional<Members> findById(Long id);

    Boolean existsByUsername(String username);

}
