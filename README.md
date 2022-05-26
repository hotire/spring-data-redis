# Spring Data Redis

study

![redis](/doc/redis_.png)


## Messaging with Redis

### Getting Started 

https://spring.io/guides/gs/messaging-redis/


```
brew install redis
```
redis 설치 
```
redis-server
```
redis start

```
redis-cli shutdown
```

redis shutdown


## Lettuce

https://github.com/lettuce-io/lettuce-core

### AbstractRedisClient#shutdown() never ends when Redis is unstable

- https://github.com/lettuce-io/lettuce-core/issues/1768

- 6.1.3 bug fix 




### Embedded Redis

임베디드 Redis로 로컬 환경에서 테스트하기 위해 사용함 

https://github.com/ozimov/embedded-redis

### Redis Failover
- enablePeriodicRefresh(default = false) : 주기적으로 cluster topology를 refresh 여부를 결정 
- enableAdaptiveRefreshTrigger(default = none) : cluster topology를 refresh를 trigger할 event를 설정할 수 있음 
  - MOVED_REDIRECT : required key가 다른 node에 있는 경우 
  - ASK_REDIRECT : slot migration이 진행되는 경우, 해당 node에 데이터가 있지만 곧 타 node로 이동될 경우 
  - PERSISTENT_RECONNECTS : connection 문제
  - UNKNOWN_NODE : 알수 없는 node
  - UNCOVERED_SLOT : 


## Pub / Sub

- https://redis.io/topics/pubsub
- https://www.baeldung.com/spring-data-redis-pub-sub
- https://brunch.co.kr/@springboot/374
- https://github.com/eugenp/tutorials/blob/master/persistence-modules/spring-data-redis/src/main/java/com/baeldung/spring/data/redis/config/RedisConfig.java