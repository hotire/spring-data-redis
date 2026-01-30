# Lifecycle

LifecycleState 상태 전환

~~~java
// CommandHandler.java
public enum LifecycleState {
    NOT_CONNECTED,      // 초기 상태
    REGISTERED,         // 핸들러 등록됨
    CONNECTED,          // TCP 연결 성립
    ACTIVATING,         // 활성화 중
    ACTIVE,             // 활성 상태 (명령어 전송 가능)
    DISCONNECTED,       // 연결 끊김
    DEACTIVATING,       // 비활성화 중
    DEACTIVATED,        // 비활성 상태
    CLOSED              // 종료 (최종 상태)
}
~~~

상태 전환 흐름

~~~
1. NOT_CONNECTED
   → ConnectionBuilder.build()
   ↓
2. REGISTERED
   → 핸들러 체인 등록
   → channelRegistered()
   ↓
3. CONNECTED
   → TCP 연결 성립
   → channelActive()
   → setState(LifecycleState.CONNECTED)
   ↓
4. ACTIVATING
   → RedisHandshakeHandler.initialize()
   → 핸드셰이크 수행 (HELLO, AUTH, SELECT 등)
   ↓
5. ACTIVE
   → 핸드셰이크 완료
   → 명령어 전송 가능
   ↓
6. DISCONNECTED
   → 네트워크 장애 또는 서버 종료
   → channelInactive()
   → setState(LifecycleState.DISCONNECTED)
   ↓
7. DEACTIVATING
   → 연결 비활성화 중
   → endpoint.notifyChannelInactive()
   ↓
8. DEACTIVATED
   → 비활성 상태
   → 명령어 버퍼에 저장
   ↓
9. CLOSED (또는 재연결)
   → close() 호출 시 CLOSED
   → 재연결 성공 시 CONNECTED로 복귀
~~~

## 재연결 ConnectionWatchdog

~~~
// ConnectionWatchdog.java
/**
 * A netty ChannelHandler responsible for monitoring the channel 
 * and reconnecting when the connection is lost.
 */
public class ConnectionWatchdog extends ChannelInboundHandlerAdapter {
    
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        channel = null;  // 연결 끊김
        
        // 자동 재연결 스케줄링
        if (listenOnChannelInactive && !reconnectionHandler.isReconnectSuspended()) {
            scheduleReconnect();
        }
    }
}
~~~

~~~
// ConnectionWatchdog.java
public void scheduleReconnect() {
    if ((channel == null || !channel.isActive()) && 
        reconnectSchedulerSync.compareAndSet(false, true)) {
        
        attempts++;
        final int attempt = attempts;
        
        // 지수 백오프 계산
        Duration delay = reconnectDelay.createDelay(attempt);
        int timeout = (int) delay.toMillis();
        
        // 타이머로 재연결 시도
        this.reconnectScheduleTimeout = timer.newTimeout(it -> {
            reconnectWorkers.submit(() -> {
                ConnectionWatchdog.this.run(attempt, delay);
                return null;
            });
        }, timeout, TimeUnit.MILLISECONDS);
    }
}
~~~

~~~
// ConnectionWatchdog.java
private void run(int attempt, Duration delay) throws Exception {
    // 1. 재연결 리스너 알림
    reconnectionListener.onReconnectAttempt(new ConnectionEvents.Reconnect(attempt));
    
    // 2. ReconnectionHandler.reconnect() 호출
    Tuple2<CompletableFuture<Channel>, CompletableFuture<SocketAddress>> tuple = 
        reconnectionHandler.reconnect();
    
    CompletableFuture<Channel> future = tuple.getT1();
    
    future.whenComplete((c, t) -> {
        if (c != null && t == null) {
            // 재연결 성공
            return;
        }
        
        // 재연결 실패
        if (!isReconnectSuspended()) {
            scheduleReconnect();  // 다시 시도
        }
    });
}
~~~

ReconnectionHandler

~~~
// ReconnectionHandler.java
protected Tuple2<CompletableFuture<Channel>, CompletableFuture<SocketAddress>> reconnect() {
    CompletableFuture<Channel> future = new CompletableFuture<>();
    
    socketAddressSupplier.subscribe(remoteAddress -> {
        // 1. 소켓 주소 가져오기
        address.complete(remoteAddress);
        
        // 2. Bootstrap.connect()로 재연결
        ChannelFuture connectFuture = bootstrap.connect(remoteAddress);
        
        connectFuture.addListener(f -> {
            if (!f.isSuccess()) {
                future.completeExceptionally(f.cause());
                return;
            }
            
            // 3. 핸드셰이크 완료 대기
            RedisHandshakeHandler handshakeHandler = 
                connectFuture.channel().pipeline().get(RedisHandshakeHandler.class);
            
            handshakeHandler.handshakeFuture().whenComplete((v, ex) -> {
                if (ex != null) {
                    future.completeExceptionally(ex);
                } else {
                    future.complete(connectFuture.channel());
                }
            });
        });
    });
    
    return Tuples.of(future, address);
}
~~~

### 흐름

~~~
1. 연결 끊김 감지
   channelInactive()
     → LifecycleState.DISCONNECTED
     → 명령어를 disconnectedBuffer에 저장
     ↓
2. 재연결 스케줄링
   ConnectionWatchdog.scheduleReconnect()
     → 지수 백오프로 지연 시간 계산
     → 타이머로 재연결 시도
     ↓
3. 재연결 시도
   ReconnectionHandler.reconnect()
     → Bootstrap.connect()
     → 핸드셰이크 수행
     ↓
4. 재연결 성공
   channelActive()
     → 상태 복원 (SELECT, AUTH 등)
     → 버퍼된 명령어 재전송
     → 정상 동작 재개
~~~

