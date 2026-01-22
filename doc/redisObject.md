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