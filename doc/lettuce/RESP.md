# RESP (Redis Serialization Protocol)
Redis 클라이언트와 서버 간 통신을 위한 직렬화 프로토콜 

- 명령어를 바이트 스트림으로 인코딩
- 응답을 바이트 스트림에서 디코딩
- 클라이언트-서버 간 통신 규약
- 클라이언트와 서버가 다른 언어/플랫폼
- 네트워크를 통해 바이트 스트림으로 통신
- 명령어와 응답을 어떻게 표현할까?

~~~
1. Simple String (+)
   형식: +문자열\r\n
   예시: +OK\r\n
   용도: 성공 메시지 (OK, PONG 등)

2. Error (-)
   형식: -에러메시지\r\n
   예시: -ERR unknown command\r\n
   용도: 에러 응답

3. Integer (:)
   형식: :숫자\r\n
   예시: :1000\r\n
   용도: 정수 값 (DEL의 반환값 등)

4. Bulk String ($)
   형식: $길이\r\n데이터\r\n
   예시: $6\r\nfoobar\r\n
   용도: 이진 안전 문자열 (GET의 반환값 등)
   특수: $-1\r\n (NULL)

5. Array (*)
   형식: *개수\r\n요소1요소2...
   예시: *2\r\n$3\r\nfoo\r\n$3\r\nbar\r\n
   용도: 여러 값의 배열 (LRANGE, MGET 등)
~~~

1. RESP 인코딩	CommandEncoder가 명령어를 RESP 형식으로 변환 
2. RESP 디코딩	RedisStateMachine이 상태 머신으로 응답 파싱

## 명령-응답 매칭

핵심: Stack (Queue) 기반 순서 매칭
~~~
// CommandHandler.java
public class CommandHandler extends ChannelDuplexHandler {
    
    private final Queue<RedisCommand<?, ?, ?>> stack;  // 명령어 대기 큐
    
    // 명령어 전송 시
    private void writeSingleCommand(ChannelHandlerContext ctx, 
                                     RedisCommand<?, ?, ?> command, 
                                     ChannelPromise promise) {
        // 1. 명령어를 stack에 추가 (순서 보장)
        addToStack(command, promise);
        
        // 2. TCP 소켓에 쓰기
        ctx.write(command, promise);
    }
    
    // 응답 수신 시
    protected void decode(ChannelHandlerContext ctx, ByteBuf buffer) {
        // 1. stack에서 첫 번째 명령어 가져오기 (FIFO)
        RedisCommand<?, ?, ?> command = stack.peek();
        
        // 2. 응답 디코딩 (해당 명령어의 output에 저장)
        if (!decode(ctx, buffer, command)) {
            return;  // 아직 완전한 응답이 아님
        }
        
        // 3. 완료되면 stack에서 제거
        stack.poll();
        
        // 4. 명령어 완료 처리
        complete(command);
    }
}
~~~
