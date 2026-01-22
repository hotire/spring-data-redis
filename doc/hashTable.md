# 해시 테이블 

두 개의 해시 테이블을 사용해 점진적 rehashing을 수행합니다.

연산	시간 복잡도	설명
- dictAdd	O(1) 평균	해시 충돌 시 체인 순회 필요
- dictFind	O(1) 평균	해시 충돌 시 체인 순회 필요
- dictDelete	O(1) 평균	체인에서 제거
- dictRehash	O(n)	하지만 점진적으로 수행

*핵심 최적화 요약*
- 점진적 rehashing: 두 테이블로 블로킹 없이 확장/축소
- 포인터 태깅: Set에서 dictEntry 없이 키만 저장 가능
- 자동 크기 조정: 사용률에 따라 자동 확장/축소
- 체이닝: 충돌 시 연결 리스트로 처리
- 메모리 프리페치: 캐시 효율 향상
이 구조로 Redis는 수백만 개의 키를 효율적으로 관리합니다. 특히 점진적 rehashing으로 서비스 중단 없이 확장할 수 있습니다.


1. dict 구조체

~~~
struct dict {
    dictType *type;              // 타입별 콜백 함수들
    
    dictEntry **ht_table[2];     // 두 개의 해시 테이블 (점진적 rehashing용)
    unsigned long ht_used[2];    // 각 테이블의 사용 중인 엔트리 수
    
    long rehashidx;              // rehashing 진행 중인 인덱스 (-1이면 rehashing 안 함)
    unsigned pauserehash;        // rehashing 일시 정지 플래그
    
    signed char ht_size_exp[2];  // 각 테이블의 크기 지수 (size = 1 << exp)
    int16_t pauseAutoResize;    // 자동 크기 조정 일시 정지
    void *metadata[];            // 메타데이터 (가변 길이)
};
~~~

2. dictEntry

~~~
struct dictEntry {
    struct dictEntry *next;  // 체이닝을 위한 다음 엔트리 포인터
    void *key;               // 키 포인터
    union {
        void *val;           // 값 포인터 (일반)
        uint64_t u64;        // 값이 정수인 경우
        int64_t s64;
        double d;
    } v;
};
~~~

## 두 개의 해시 테이블: 점진적 Rehashing

왜 두 개의 테이블을 사용하나?

### 일반적인 rehashing:

~~~
// 나쁜 방법: 한 번에 모든 엔트리 이동
void badRehash(dict *d) {
    // 1. 새 테이블 할당
    dictEntry **new_table = malloc(new_size);
    
    // 2. 모든 엔트리를 새 테이블로 이동 (블로킹!)
    for (int i = 0; i < old_size; i++) {
        // 모든 엔트리 이동... (시간이 오래 걸림)
    }
    
    // 3. 이 동안 Redis는 멈춤! 😱
}
~~~

### Redis의 점진적 rehashing:

~~~
// 좋은 방법: 조금씩 이동
void incrementalRehash(dict *d) {
    // 1. ht_table[0] (기존), ht_table[1] (새 테이블) 둘 다 사용
    // 2. 조금씩 (예: 1개 버킷씩) 이동
    // 3. 이 동안에도 Redis는 정상 동작! ✅
}
~~~

###  핵심 동작 원리

~~~
Rehashing 진행 중:

ht_table[0] (기존)          ht_table[1] (새)
┌─────────┐                ┌─────────┐
│ [0] ──→ │                │ [0] ──→ │  (이미 이동됨)
│ [1] ──→ │                │ [1] ──→ │
│ [2] ──→ │ ← rehashidx    │ [2] ──→ │  (이동 중)
│ [3] ──→ │                │ [3]     │
│ [4] ──→ │                │ [4]     │  (아직 이동 안 됨)
└─────────┘                └─────────┘
~~~


## Rehasing (feat scan)

- https://tech.kakao.com/posts/314

Redis는 특정 사이즈가 넘을 때 마다 Bucket을 두 배로 확장하고, 
Key들을 rehash하게 됩니다. 먼저 이 때 Key의 Hash로 사용하는 해시함수는 다음과 같습니다. MurmurHash2를 사용합니다.

table에는 Key를 찾기위해 비트 연산을 하기 위한 sizemask가 들어가 있습니다. 초기에는 table의 bucket이 4개 이므로 sizemask는 이진수로 11 즉 3의 값이 셋팅됩니다. 즉 해시된 결과 & 11의 연산결과로 들어가야 하는 Bucket이 결정되게 됩니다.
여기서 Key가 많아지면 Redis는 Table의 사이즈를 2배로 늘리게 됩니다. 그러면 당연히 sizemask도 커지게 됩니다. Table size가 8이면 sizemask는 7이 됩니다.

먼저 간단하게 말하자면, SCAN의 원리는 이 Bucket을 한 턴에 하나씩 순회하는 것입니다. 그래서 아래 그림과 같이 처음에는 Bucket Index 0를 읽고 데이터를 던져주는 것입니다.
이번에는 Redis SCAN의 동작을 더 분석하기 위해서 Redis Hash Table의 Rehashing과, 그 상황에서 SCAN이 어떻게 동작하는지 알아보도록 하겠습니다. 앞에서도 간단하게 언급했지만 Redis Hash Table은 보통 Dynamic Bucket에 충돌은 list로 처리하는 방식입니다.

2배로 테이블이 늘어나면서, bitmask는 하나 더 사용하도록 됩니다. 이렇게 테이블이 확장되면 Rehash를 하게 됩니다. 그래야만 검색시에 제대로 찾을 수 있기 때문입니다. 먼저 Table을 확장할 때 사용하는 것이 _dictExpandIfNeeded 합수입니다. dictIsRehashing는 이미 Rehash 중인지를 알려주는 함수이므로, Rehashing 중이면 이미 테이블이 확장된 상태이므로 그냥 DICT_OK를 리턴합니다.

한꺼번에 모든 테이블을 Rehashing 해야 하면 당연히 시간이 많이 걸립니다. **O(n)**의 시간이 필요합니다. 그래서 Redis는 rehash flag와 rehashidx라는 변수를 이용해서, hash table에서 하나씩 Rehash하게 됩니다. 
즉, 확장된 크기가 8이라면 이전 크기 총 4번의 Rehash 스텝을 통해서 Rehashing이 일어나게 됩니다. (이로 인해서 뒤에서 설명하는 특별한 현상이 생깁니다.)

그리고 현재 rehashing 중인것을 체크하는 함수가 dictIsRehashing 함수입니다. rehashidx가 -1이 아니면 Rehashing 중인 상태입니다.

~~~
~~~
그럼 의문이 생깁니다. 
1. Rehashing 중에 추가 되는 데이터는?
- 이 때는 무조건 ht[1]으로 들어가게 됩니다.
2. 검색이나 삭제, 업데이트는?? 
- 이 때는 ht[0], ht[1]을 모두 탐색하게 됩니다.(어쩔 수 없겠죠?)



### eviction loop

https://velog.io/@ma2sql/Rehashing%EA%B3%BC-big-eviction-loop




