# RedisClient

1. 싱글톤처럼 사용
    - 애플리케이션당 하나 또는 공유
    - 비용이 큰 리소스 (Netty 인프라)

2. 여러 연결 생성 가능
    - connect()를 여러 번 호출 가능
    - 각각 독립적인 Connection

3. 생명주기가 길음
    - 애플리케이션 시작 시 생성
    - 애플리케이션 종료 시 shutdown()

## RedisClient vs Connection 차이

~~~
RedisClient = 공장 (Factory)
  - 연결을 만드는 공장
  - Netty 인프라 관리
  - 여러 연결을 생성 가능

Connection = 실제 통신선 (TCP 연결)
  - 실제 Redis 서버와 통신
  - 명령 전송/응답 수신
  - 상태 관리 (연결 상태, 트랜잭션 등)
~~~

### RedisClient

~~~
RedisClient
├─ AbstractRedisClient
│  ├─ ChannelGroup channels  (모든 연결 채널 관리)
│  ├─ EventLoopGroup eventLoopGroups  (Netty 이벤트 루프)
│  ├─ ClientResources clientResources  (스레드 풀, 메트릭)
│  └─ ClientOptions clientOptions  (옵션)
└─ RedisURI redisURI  (기본 URI)
~~~

생명주기 
- 애플리케이션 시작 시 생성
- 애플리케이션 종료 시 shutdown()
- 생명주기: 애플리케이션 전체

리소스 관리
- Netty EventLoopGroup (스레드 풀)
- ChannelGroup (모든 채널 관리)
- ClientResources (공유 리소스)
  → 비용이 큰 리소스

### Connection
생명주기 
- 필요할 때 생성 (client.connect())
- 사용 후 close() 가능
- 생명주기: 작업 단위 또는 세션

리소스 관리
- TCP 소켓
- 명령 큐
- 연결 상태
  → 상대적으로 가벼운 리소스

~~~
StatefulRedisConnectionImpl
├─ RedisCodec<K, V> codec  (인코딩/디코딩)
├─ ConnectionState state  (연결 상태)
├─ PushHandler pushHandler  (Push 메시지)
├─ RedisCommands sync  (동기 API)
├─ RedisAsyncCommands async  (비동기 API)
└─ RedisReactiveCommands reactive  (리액티브 API)
~~~


## Connection: 실제 통신 채널

1. 실제 TCP 연결
    - Redis 서버와 직접 통신
    - 소켓 레벨 연결

2. 상태 관리
    - 연결 상태 (연결됨/끊김)
    - 트랜잭션 상태 (MULTI 중인지)
    - 데이터베이스 선택 상태

3. 여러 API 제공
    - sync(): 동기 API
    - async(): 비동기 API
    - reactive(): 리액티브 API
    - 모두 같은 연결 사용!

## 관계, 생성

~~~
// 1. RedisClient 생성
RedisClient client = RedisClient.create("redis://localhost:6379");
  ↓
// AbstractRedisClient 생성
// - Netty EventLoopGroup 생성
// - ChannelGroup 생성
// - ClientResources 설정
  ↓
// 2. Connection 생성
StatefulRedisConnection<String, String> connection = client.connect();
  ↓
// RedisClient.connect() 내부:
// - connectStandaloneAsync() 호출
// - DefaultEndpoint 생성 (Netty 채널)
// - StatefulRedisConnectionImpl 생성
// - TCP 연결 설정
// - AUTH, SELECT 등 초기화
  ↓
// 3. Connection 사용
RedisCommands<String, String> sync = connection.sync();
sync.set("key", "value");
~~~


## EventLoopGroup vs EventLoop

EventLoopGroup = 여러 EventLoop의 그룹
- TCP 연결용: NioEventLoopGroup (또는 EpollEventLoopGroup)
- Unix Domain Socket용: EpollEventLoopGroup
- 각 EventLoopGroup은 여러 스레드(EventLoop)를 포함


~~~
RedisClient (1개)
│
├─ EventLoopGroup (TCP용) - 1개
│   ├─ EventLoop #1 (스레드 1)
│   │   ├─ Connection #1
│   │   ├─ Connection #2
│   │   └─ Connection #3
│   │
│   ├─ EventLoop #2 (스레드 2)
│   │   ├─ Connection #4
│   │   └─ Connection #5
│   │
│   ├─ EventLoop #3 (스레드 3)
│   │   └─ Connection #6
│   │
│   └─ EventLoop #4 (스레드 4)
│       └─ Connection #7
│
└─ EventLoopGroup (Unix Socket용) - 1개 (선택적)
    └─ EventLoop #1, #2, ...
~~~

## RedisClient.shutdown() 동작 과정

~~~
1단계: closeResources()
   ├─ closeableResources 닫기 (Connection 등)
   ├─ ChannelGroup의 모든 Channel 닫기
   └─ ConnectionWatchdog 비활성화

2단계: closeClientResources()
   ├─ EventLoopGroup shutdown
   ├─ EventExecutorGroup shutdown
   └─ 기타 리소스 정리
~~~

### Graceful Shutdown 파라미터

~~~
// 기본 shutdown
client.shutdown();
// → quietPeriod: 0초
// → timeout: 2초

// 커스텀 shutdown
client.shutdown(Duration.ofSeconds(1), Duration.ofSeconds(5));
// → quietPeriod: 1초 (새 작업 없이 대기)
// → timeout: 5초 (최대 대기 시간)
~~~

quietPeriod의 의미

~~~
quietPeriod: "조용한 기간"
  - 이 시간 동안 새로운 작업이 없으면 graceful shutdown 시작
  - 새로운 작업이 들어오면 quietPeriod 다시 시작
  - 모든 작업이 완료될 때까지 기다림

timeout: "최대 대기 시간"
  - quietPeriod가 지나도 작업이 완료되지 않으면
  - timeout 후 강제 종료
~~~


## StatefulConnection

✅ StatefulConnection = "상태를 유지하는 연결"

- 연결이 여러 상태 정보를 기억하고 관리함
- 각 명령어 실행 간에 상태가 유지됨
- 재연결 시 상태를 복원할 수 있음

1. 연결 상태 (연결됨/끊김)
2. 데이터베이스 선택 상태 (SELECT 명령)
3. 트랜잭션 상태 (MULTI/EXEC)
4. 인증 상태 (AUTH)
5. 읽기 전용 모드 (READONLY)
6. 타임아웃 설정
7. Auto-flush 설정
8. Pending commands

### 상태 연결 예

예시 1: 데이터베이스 선택 상태
~~~
StatefulRedisConnection<String, String> conn = client.connect();

// DB 0에서 작업
conn.sync().set("key1", "value1");

// DB 1로 변경
conn.sync().select(1);  // 상태 변경!

// 이후 모든 명령은 DB 1에서 실행됨
conn.sync().set("key2", "value2");  // DB 1에 저장
conn.sync().get("key1");  // DB 1에서 조회 (key1 없음)

// ✅ Connection이 SELECT 상태를 기억함!
~~~

예시 2: 트랜잭션 상태

~~~
StatefulRedisConnection<String, String> conn = client.connect();

conn.sync().multi();  // 트랜잭션 시작 (상태 변경)
conn.sync().set("key1", "value1");
conn.sync().set("key2", "value2");

boolean isInTransaction = conn.isMulti();  // true

conn.sync().exec();  // 트랜잭션 커밋 (상태 해제)

isInTransaction = conn.isMulti();  // false

// ✅ Connection이 MULTI 상태를 기억함!
~~~

### 실제 사용 예시

~~~
RedisClient client = RedisClient.create("redis://localhost:6379");
StatefulRedisConnection<String, String> conn = client.connect();

// 1. 데이터베이스 선택 (상태 저장)
conn.sync().select(1);
System.out.println("DB: " + conn.getConnectionState().getDb());  // 1

// 2. 읽기 전용 모드 (상태 저장)
conn.sync().readonly();
System.out.println("ReadOnly: " + conn.getConnectionState().isReadOnly());  // true

// 3. 트랜잭션 시작 (상태 저장)
conn.sync().multi();
System.out.println("In Transaction: " + conn.isMulti());  // true

// 4. 명령 실행 (상태 유지)
conn.sync().set("key", "value");

// 5. 트랜잭션 커밋 (상태 해제)
conn.sync().exec();
System.out.println("In Transaction: " + conn.isMulti());  // false
~~~

## RedisTemplate에서 Connection 생명주기

✅ 명령어마다 Connection을 얻고, 명령어 실행 후 close()를 호출합니다!

하지만:
- shareNativeConnection = true (기본값)인 경우:
  → LettuceConnection.close()는 호출되지만
  → 실제 StatefulRedisConnection은 닫히지 않고 재사용됨

- Transaction 중인 경우:
  → Connection이 유지되어 여러 명령어에 걸쳐 사용됨

~~~
// RedisTemplate.java
public <T> T execute(RedisCallback<T> action, boolean exposeConnection, boolean pipeline) {
    RedisConnectionFactory factory = getRequiredConnectionFactory();
    
    // 1. Connection 얻기
    RedisConnection conn = RedisConnectionUtils.getConnection(factory, enableTransactionSupport);
    
    try {
        // 2. 명령어 실행
        T result = action.doInRedis(connToExpose);
        return result;
    } finally {
        // 3. Connection 해제 (명령어 실행 후 항상 호출)
        RedisConnectionUtils.releaseConnection(conn, factory);
    }
}
~~~

~~~
// LettuceConnectionFactory.java
public RedisConnection getConnection() {
    // shareNativeConnection = true (기본값)이면
    // 공유된 StatefulRedisConnection을 가져옴
    StatefulRedisConnection<byte[], byte[]> sharedConnection = getSharedConnection();
    
    // LettuceConnection을 생성 (공유 Connection을 래핑)
    LettuceConnection connection = doCreateLettuceConnection(
        sharedConnection,  // 공유된 실제 Connection
        this.connectionProvider,
        getTimeout(),
        getDatabase()
    );
    
    return connection;
}
~~~

shareNativeConnection = true인 경우

~~~
명령어 1: getConnection() → LettuceConnection 생성 (공유 StatefulRedisConnection 사용)
명령어 2: getConnection() → LettuceConnection 생성 (같은 공유 StatefulRedisConnection 사용)
명령어 3: getConnection() → LettuceConnection 생성 (같은 공유 StatefulRedisConnection 사용)
~~~

Connection 해제 (releaseConnection)
~~~
// RedisConnectionUtils.java
public static void releaseConnection(RedisConnection conn, RedisConnectionFactory factory) {
    RedisConnectionHolder conHolder = TransactionSynchronizationManager.getResource(factory);
    
    if (conHolder != null) {
        // Transaction이 활성화되어 있으면 Connection을 닫지 않음
        if (conHolder.isTransactionActive()) {
            conHolder.released();
            return;  // Connection 유지!
        }
        
        // Transaction이 없으면 unbind 후 닫기
        unbindConnection(factory);
        return;
    }
    
    // Thread에 바인딩되지 않은 Connection은 바로 닫기
    doCloseConnection(conn);
}

private static void doCloseConnection(RedisConnection connection) {
    connection.close();  // LettuceConnection.close() 호출
}
~~~