package com.github.hotire.spring.data.redis.core.repository;

import java.util.List;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class RedisRepositoryComposite {
    private final List<CrudRepository<?, ?>> repositories;

    @PostConstruct
    public void init() {
        log.info("repositories : {}", repositories);
    }
}
