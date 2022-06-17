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

## 메모리 운영기법

- https://ssoco.tistory.com/16

### Maxmemory

메모리 사용량 제한을 위해 사용하는 maxmemory는 32bit/64bit 환경에 따라 초기값이 다르게 설정된다. 32bit 환경에서는 초기값이 3GB로 설정되어 최대 3GB 메모리만 사용 가능한 반면에, 64bit 환경에서는 초기값이 0으로 설정된다. 즉, 64bit 환경에서는 메모리 사용량 제한이 없으며 운영체제의 가상메모리(스왑)까지 사용한다.(이 때 시스템의 메모리 한계를 인식하지 못해 더 많은 메모리를 요구하여 문제가 발생할 수 있기 때문에 따로 설정을 해주어야 한다.)

### Maxmemory-policy 

max-memory만큼 메모리를 사용하게 되면, 메모리 정책에 따라 과거에 만들어진 키들이 삭제된다.

1. noeviction
: 캐시를 지우지 않는 정책이다. 메모리가 maxmemory 이상을 사용하게 되면 error를 발생시킨다.

2. allkey 
: 각 정책에 따라 모든 키를 대상으로 정리한다.
    - allkeys-lru : LRU 알고리즘 기반으로 키를 삭제한다.
    - allkey-random : 랜덤하게 키를 삭제한다.
    - allkeys-lfu : LFU 알고리즘 기반으로 키를 삭제한다.
3. volatile 
: 각 정책에 따라 EXPIRE SET에 있는 키들을 대상으로 정리한다.
    - volatile-lru : LRU 알고리즘 기반으로 키를 삭제한다.
    - volatile-random : 랜덤하게 키를 삭제한다.
    - volatile-ttl : TTL이 짧은 순으로 삭제한다.
    - volatile-lfu : LFU 알고리즘 기반으로 키를 삭제한다.

    
    

### interview 

- https://intrepidgeeks.com/tutorial/summary-and-explanation-of-common-interview-questions-of-redis

## Pub / Sub

- https://redis.io/topics/pubsub
- https://www.baeldung.com/spring-data-redis-pub-sub
- https://brunch.co.kr/@springboot/374
- https://github.com/eugenp/tutorials/blob/master/persistence-modules/spring-data-redis/src/main/java/com/baeldung/spring/data/redis/config/RedisConfig.java