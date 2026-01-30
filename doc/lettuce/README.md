# Lettuce

핵심: Redis 서버 지식을 클라이언트 관점으로 연결

~~~
Redis 서버 
  ↓
Lettuce 클라이언트 
  - 서버와 어떻게 통신하는가?
  - 명령을 어떻게 전송하는가?
  - 응답을 어떻게 파싱하는가?
~~~

## 1단계: 기본 구조 이해

학습 포인트:
- RedisClient vs Connection의 차이
- StatefulConnection의 의미
- 동기/비동기/리액티브 API 차이

~~~
1. RedisClient.java
   - 클라이언트 생성과 생명주기
   - ClientResources 관리
   - 연결 생성

2. StatefulRedisConnection.java / StatefulRedisConnectionImpl.java
   - 연결 상태 관리
   - 동기/비동기/리액티브 API 제공
   - 연결 생명주기

3. RedisURI.java
   - 연결 정보 표현
   - URI 파싱

4. docs/getting-started.md
   - 기본 사용법
~~~

## 2단계: 프로토콜 레이어 

- RESP 프로토콜 인코딩/디코딩
- 명령-응답 매칭
- 비동기 처리 방식

~~~
1. protocol/Command.java
   - 명령 객체 구조
   - 명령 생성과 전송

2. protocol/CommandHandler.java
   - 명령 전송 처리
   - 응답 수신 처리

3. protocol/RedisStateMachine.java
   - RESP 프로토콜 파싱
   - 상태 머신으로 응답 파싱

4. protocol/CommandEncoder.java
   - 명령을 RESP 형식으로 인코딩

5. protocol/DefaultEndpoint.java
   - Netty 채널과의 연결
   - 명령 디스패치
~~~

## 3단계: 연결 관리

연결 생명주기와 재연결 메커니즘
- 연결 끊김 감지
- 자동 재연결
- 대기 중인 명령 재전송
~~~
1. protocol/ConnectionWatchdog.java
   - 연결 모니터링
   - 자동 재연결

2. protocol/ReconnectionHandler.java
   - 재연결 로직
   - 재연결 전략

3. protocol/ConnectionInitializer.java
   - 연결 초기화
   - AUTH, SELECT 등

4. ConnectionBuilder.java
   - 연결 생성 과정
   - Netty 채널 설정
~~~

## 4단계: 명령 인터페이스 
- 동기 API: Future.get()으로 블로킹
- 비동기 API: CompletableFuture 사용
- 리액티브 API: Mono/Flux 사용
~~~
1. api/sync/RedisCommands.java
   - 동기 API 인터페이스
   - RedisAsyncCommandsImpl.java (구현)

2. api/async/RedisAsyncCommands.java
   - 비동기 API 인터페이스
   - RedisAsyncCommandsImpl.java (구현)

3. api/reactive/RedisReactiveCommands.java
   - 리액티브 API 인터페이스
   - RedisReactiveCommandsImpl.java (구현)

4. RedisFuture.java
   - 비동기 결과 표현
~~~

## 5단계: Pub/Sub 구현

- 서버의 Pub/Sub와 클라이언트 처리 연결
- Push 메시지 처리
- 리스너 패턴

~~~
1. pubsub/StatefulRedisPubSubConnection.java
   - Pub/Sub 연결

2. pubsub/RedisPubSubListener.java
   - 메시지 리스너

3. pubsub/PubSubEndpoint.java
   - Pub/Sub 명령 처리

4. protocol/PushHandler.java
   - Push 메시지 처리
~~~

## 6단계: 클러스터 지원 

- 슬롯 기반 라우팅
- MOVED/ASK 리다이렉션 처리
- 토폴로지 갱신

~~~
1. cluster/RedisClusterClient.java
   - 클러스터 클라이언트

2. cluster/ClusterTopologyRefresh.java
   - 토폴로지 갱신

3. cluster/ClusterCommandHandler.java
   - 클러스터 명령 라우팅

4. cluster/Partitions.java
   - 슬롯-노드 매핑
~~~

## 7단계: 고급 기능 

- 코덱 커스터마이징
- 성능 튜닝
- 모니터링

~~~
1. codec/RedisCodec.java
   - 인코딩/디코딩
   - 커스텀 코덱

2. ClientOptions.java
   - 클라이언트 옵션

3. ClientResources.java
   - 리소스 관리
   - EventLoopGroup 관리

4. metrics/ (선택)
   - 메트릭 수집
~~~