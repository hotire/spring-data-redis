# Pubsub

구독 흐름
~~~
1. 연결 생성
   RedisClient.connectPubSub()
     → StatefulRedisPubSubConnection 생성
     → PubSubEndpoint 생성
     ↓
2. 리스너 등록
   connection.addListener(listener)
     → PubSubEndpoint.listeners에 추가
     ↓
3. 구독 명령어 전송
   sync.subscribe("channel")
     → SUBSCRIBE 명령어 전송
     → PubSubEndpoint.write()
     → subscribeWritten = true
     ↓
4. 구독 응답 수신
   PubSubCommandHandler.decode()
     → RESP 디코딩: ["subscribe", "channel", 1]
     → PubSubMessage 생성
     → endpoint.notifyMessage()
     → listener.subscribed("channel", 1)
     → channels.add("channel")
~~~

메시지 수신 흐름

~~~
1. Redis 서버에서 메시지 발행
   PUBLISH channel "hello"
     ↓
2. 클라이언트로 Push 메시지 전송
   RESP2: *3\r\n$7\r\nmessage\r\n$7\r\nchannel\r\n$5\r\nhello\r\n
   RESP3: >4\r\n+pubsub\r\n$7\r\nmessage\r\n$7\r\nchannel\r\n$5\r\nhello\r\n
     ↓
3. PubSubCommandHandler.decode()
   → isPushDecode(buffer) = true
   → PubSubOutput으로 디코딩
   → PubSubMessage 생성
     ↓
4. 리스너 알림
   endpoint.notifyMessage(message)
     → listener.message("channel", "hello")
     → 애플리케이션 콜백 실행
~~~

## StatefulRedisPubSubConnection

✅ StatefulRedisPubSubConnection은 StatefulRedisConnection을 상속받지만,
Pub/Sub 전용 기능이 추가된 특수한 연결 타입입니다!

주요 차이:
1. PubSubEndpoint 사용 (구독 상태 관리)
2. RedisPubSubListener 사용 (메시지 수신)
3. 재연결 시 자동 구독 복원
4. 구독 후 일반 명령어 사용 불가
5. PubSubCommandHandler 사용 (Push 메시지 처리)

## CommandHandler
~~~
// CommandHandler.java
protected void decode(ChannelHandlerContext ctx, ByteBuf buffer) 
    throws InterruptedException {
    
    // 1. 스택이 비어있고 Push 메시지가 아니면 무시
    if (pristine) {
        if (stack.isEmpty() && buffer.isReadable() && !isPushDecode(buffer)) {
            // 명령어 없이 응답이 온 경우 (비정상)
            consumeResponse(buffer);
            return;
        }
    }
    
    // 2. 메인 디코딩 루프
    while (canDecode(buffer)) {
        
        // ✅ Push 메시지인지 확인!
        if (isPushDecode(buffer)) {
            // Push 메시지 처리 (Pub/Sub 등)
            decode(ctx, buffer, pushOutput);
            notifyPushListeners(output);
            
        } else {
            // ✅ 일반 명령어 응답 처리
            RedisCommand<?, ?, ?> command = stack.peek();
            decode(ctx, buffer, command);
            
            if (canComplete(command)) {
                stack.poll();
                complete(command);  // 명령어 완료
            }
        }
    }
}
~~~
