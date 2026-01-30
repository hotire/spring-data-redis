# redisObject

Redis에서 값이 저장되는 방식:

┌─────────────────────────────────────┐
│      redisObject (robj)              │
│  ┌───────────────────────────────┐  │
│  │ type: OBJ_STRING              │  │
│  │ encoding: OBJ_ENCODING_RAW    │  │
│  │ refcount: 1                   │  │
│  │ ptr: ────────────────────┐    │  │
│  └───────────────────────────────┘  │
│                                     │
│         ptr가 가리키는 곳            │
│              ↓                      │
│      ┌───────────────┐             │
│      │  SDS string   │             │
│      │  "hello"      │             │
│      └───────────────┘             │
└─────────────────────────────────────┘


1. Redis의 모든 값은 redisObject로 감싸집니다
- Redis 내부에서   robj *val = createStringObject("hello", 5);   // val->ptr이 SDS를 가리킴
2. 타입과 인코딩을 관리합니다
- type: STRING, LIST, SET, HASH, ZSET 등
- encoding: 실제 저장 방식 (RAW, EMBSTR, INT, HT 등)
3. 메모리 관리의 핵심입니다
- 참조 카운팅 (refcount)
- LRU/LFU 정보 (lru)

~~~
struct redisObject {
    unsigned type:4;        // 타입 (STRING, LIST, SET 등)
    unsigned encoding:4;   // 인코딩 (RAW, EMBSTR, INT 등)
    unsigned refcount : 23; // 참조 카운팅
    unsigned iskvobj : 1;   // kvobj 여부
    unsigned metabits :8;  // 메타데이터 비트맵
    unsigned lru:24;        // LRU/LFU 정보
    void *ptr;             // 실제 데이터 포인터 (SDS, dict 등)
};
~~~

~~~
// 메모리 효율성:
// type: 4비트 (16가지 타입)
// encoding: 4비트 (16가지 인코딩)
// refcount: 23비트 (최대 8,388,607)
// iskvobj: 1비트 (0 또는 1)
// metabits: 8비트 (256가지 조합)
// lru: 24비트

// 총: 4 + 4 + 23 + 1 + 8 + 24 = 64비트 (8바이트)
// + ptr: 8바이트 (64비트 시스템)
// = 총 16바이트

~~~

### 인코딩

1. OBJ_ENCODING_INT
    - 작은 정수는 포인터에 직접 저장
    - ptr = (void*)(long)value
    - 메모리 절약!

2. OBJ_ENCODING_EMBSTR
    - 작은 문자열 (44바이트 이하)
    - 객체와 문자열을 같은 메모리 블록에 저장
    - 캐시 효율적!

3. OBJ_ENCODING_RAW
    - 큰 문자열
    - 별도 메모리에 SDS 저장
    - ptr → SDS 포인터

### robj(일반객체) vs kvobj (키-값 객체)

데이터베이스에 저장된 값 = kvobj
임시 객체, 응답, 복제/AOF = robj

1. 메모리 효율성: 키를 객체 내부에 임베드 (별도 할당 불필요)
2. 캐시 효율성: 키와 값이 같은 메모리 블록 (캐시 친화적)
3. 메타데이터 지원: 만료 시간, 모듈 메타데이터 등
4. 유연성: robj는 임시 객체에 사용 (키 정보 불필요)ㅇ


## ETC
# 메모리 사용량 추정하기
- https://mangkyu.tistory.com/417