# Pub/Sub

Pub/Sub = Publish/Subscribe (발행/구독)
- 발행자(Publisher): 메시지를 채널에 발행
- 구독자(Subscriber): 채널을 구독하고 메시지 수신
- 채널(Channel): 메시지가 전달되는 경로

**특징**
1. 비동기 메시징: 발행자와 구독자가 직접 연결되지 않음
2. 1:N 통신: 하나의 메시지가 여러 구독자에게 전달
3. 실시간: 메시지가 즉시 전달됨
4. 휘발성: 메시지는 저장되지 않음 (전달 후 사라짐)

## 구조

### 서버 

~~~
// server.h - redisServer 구조체
struct redisServer {
    // 일반 채널 (SUBSCRIBE)
    kvstore *pubsub_channels;  
    // 키: 채널 이름 (robj*)
    // 값: 해당 채널을 구독하는 클라이언트 딕셔너리 (dict*)
    
    // 패턴 채널 (PSUBSCRIBE)
    dict *pubsub_patterns;  
    // 키: 패턴 (robj*)
    // 값: 해당 패턴을 구독하는 클라이언트 딕셔너리 (dict*)
    
    // 샤드 레벨 채널 (SSUBSCRIBE) - 클러스터 모드
    kvstore *pubsubshard_channels;  
    // 슬롯별로 관리되는 샤드 채널
    
    unsigned int pubsub_clients;  // Pub/Sub 모드 클라이언트 수
};
~~~

### 클라이언트
~~~
// server.h - client 구조체
struct client {
    // 클라이언트가 구독한 채널들
    dict *pubsub_channels;  
    // 키: 채널 이름 (robj*)
    // 값: NULL (값은 사용 안 함)
    
    // 클라이언트가 구독한 패턴들
    dict *pubsub_patterns;  
    // 키: 패턴 (robj*)
    // 값: NULL
    
    // 클라이언트가 구독한 샤드 채널들
    dict *pubsubshard_channels;  
    // 키: 샤드 채널 이름 (robj*)
    // 값: NULL
    
    int flags;  // CLIENT_PUBSUB 플래그 포함
};
~~~

## 구독 매커니즘

~~~
// pubsub.c - subscribeCommand()
void subscribeCommand(client *c) {
    for (j = 1; j < c->argc; j++)
        pubsubSubscribeChannel(c, c->argv[j], pubSubType);
    markClientAsPubSub(c);
}
~~~

~~~
// pubsub.c - pubsubSubscribeChannel()
int pubsubSubscribeChannel(client *c, robj *channel, pubsubtype type) {
    // 1. 클라이언트가 이미 구독했는지 확인
    if (link == NULL) {  // 아직 구독 안 함
        // 2. 서버의 채널 → 클라이언트 딕셔너리에 추가
        de = kvstoreDictAddRaw(*type.serverPubSubChannels, slot, channel, &existing);
        
        if (existing) {
            // 채널이 이미 존재 → 기존 클라이언트 딕셔너리 가져오기
            clients = dictGetVal(existing);
        } else {
            // 채널이 없음 → 새 클라이언트 딕셔너리 생성
            clients = dictCreate(&clientDictType);
            kvstoreDictSetVal(*type.serverPubSubChannels, slot, de, clients);
        }
        
        // 3. 클라이언트를 딕셔너리에 추가
        dictAdd(clients, c, NULL);
        
        // 4. 클라이언트의 채널 딕셔너리에 추가
        dictSetKeyAtLink(type.clientPubSubChannels(c), channel, &bucket, 1);
    }
    
    // 5. 구독 확인 메시지 전송
    addReplyPubsubSubscribed(c, channel, type);
    // 응답: ["subscribe", "news", 1]
}
~~~

### 예시
- 예시: SUBSCRIBE news
~~~
1. 클라이언트: SUBSCRIBE news
   ↓
2. 서버 처리:
   - server.pubsub_channels["news"] 확인
   - 없으면: dict 생성 → server.pubsub_channels["news"] = dict {}
   - client1을 dict에 추가: dict {client1}
   - client1.pubsub_channels["news"] = NULL 추가
   ↓
3. 응답: ["subscribe", "news", 1]
   (1 = 현재 구독한 채널 개수)
~~~


## 발행 매커니즘

~~~
// pubsub.c - publishCommand()
void publishCommand(client *c) {
    int receivers = pubsubPublishMessageAndPropagateToCluster(
        c->argv[1],  // channel
        c->argv[2],  // message
        0            // sharded = 0 (일반 채널)
    );
    addReplyLongLong(c, receivers);  // 수신자 수 반환
}
~~~


~~~
int pubsubPublishMessageInternal(robj *channel, robj *message, pubsubtype type) {
    int receivers = 0;
    
    // 1. 채널 구독자에게 전송
    de = kvstoreDictFind(*type.serverPubSubChannels, slot, channel);
    if (de) {
        dict *clients = dictGetVal(de);
        dictInitIterator(&iter, clients);
        while ((entry = dictNext(&iter)) != NULL) {
            client *c = dictGetKey(entry);
            addReplyPubsubMessage(c, channel, message, *type.messageBulk);
            // 응답: ["message", "news", "Hello"]
            receivers++;
        }
    }
    
    // 2. 패턴 구독자에게 전송 (일반 채널만)
    if (!type.shard && dictSize(server.pubsub_patterns) > 0) {
        // 모든 패턴 확인
        dictInitIterator(&di, server.pubsub_patterns);
        while((de = dictNext(&di)) != NULL) {
            robj *pattern = dictGetKey(de);
            // 패턴 매칭 확인
            if (stringmatchlen(...)) {
                // 매칭됨 → 구독자에게 전송
                dict *clients = dictGetVal(de);
                // ... 모든 클라이언트에게 전송
                receivers++;
            }
        }
    }
    
    return receivers;
}
~~~

### 예시: PUBLISH news "Hello"

~~~
1. 클라이언트: PUBLISH news "Hello"
   ↓
2. 서버 처리:
   a) 채널 구독자 확인:
      - server.pubsub_channels["news"] = dict {client1, client2}
      - client1에게: ["message", "news", "Hello"]
      - client2에게: ["message", "news", "Hello"]
      receivers = 2
   
   b) 패턴 구독자 확인:
      - server.pubsub_patterns["news.*"] = dict {client3}
      - "news"가 "news.*"와 매칭됨
      - client3에게: ["pmessage", "news.*", "news", "Hello"]
      receivers = 3
   ↓
3. 응답: (integer) 3
~~~

## 전체 흐름

### 예시

~~~
시나리오: 3개 클라이언트가 "news" 채널 구독

1. 클라이언트1: SUBSCRIBE news
   → server.pubsub_channels["news"] = dict {client1}
   → client1.pubsub_channels["news"] = NULL
   → 응답: ["subscribe", "news", 1]

2. 클라이언트2: SUBSCRIBE news
   → server.pubsub_channels["news"] = dict {client1, client2}
   → client2.pubsub_channels["news"] = NULL
   → 응답: ["subscribe", "news", 1]

3. 클라이언트3: PSUBSCRIBE news.*
   → server.pubsub_patterns["news.*"] = dict {client3}
   → client3.pubsub_patterns["news.*"] = NULL
   → 응답: ["psubscribe", "news.*", 1]

4. 클라이언트4: PUBLISH news "Breaking news!"
   → server.pubsub_channels["news"] 확인
     - client1에게: ["message", "news", "Breaking news!"]
     - client2에게: ["message", "news", "Breaking news!"]
   → server.pubsub_patterns["news.*"] 확인
     - "news"가 "news.*"와 매칭
     - client3에게: ["pmessage", "news.*", "news", "Breaking news!"]
   → 응답: (integer) 3
~~~


## CLIENT_PUSHING

1. 응답 순서 제어
    - MULTI-EXEC 중 push 메시지를 나중에 전송
    - 명령 응답과 push 메시지 구분

2. 응답 비활성화 우회
    - CLIENT_REPLY_OFF를 설정해도
    - Push 메시지는 전송해야 함

3. 프로토콜 형식 구분
    - RESP2: 일반 배열
    - RESP3: Push 형식 (>)
    - 플래그로 구분 가능

4. 메시지 타입 구분
    - 일반 명령 응답 vs Push 메시지
    - 서버가 자발적으로 보내는 메시지 표시


### MULTI-EXEC

MULTI-EXEC = Redis의 트랜잭션 메커니즘
- 여러 명령을 하나의 원자적 단위로 묶어서 실행
- 모든 명령이 성공하거나 모두 실패 (원자성)
- 중간에 다른 클라이언트의 명령이 끼어들 수 없음
