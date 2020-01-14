package com.github.hotire.spring.data.redis.embedded_redis;

import org.springframework.data.repository.CrudRepository;


public interface PointRedisRepository extends CrudRepository<Point, String> {
}

