# SDS(Simple Dynamic String)
핵심 아이디어: 문자열 길이에 맞는 헤더 타입 선택
SDS는 문자열 길이에 따라 5가지 헤더 타입을 사용

- SDS_TYPE_5  → 최대 31바이트 (1바이트 헤더)
- SDS_TYPE_8  → 최대 255바이트 (3바이트 헤더)
- SDS_TYPE_16 → 최대 65,535바이트 (5바이트 헤더)
- SDS_TYPE_32 → 최대 4GB (9바이트 헤더)
- SDS_TYPE_64 → 최대 16EB (17바이트 헤더)

## 메모리 레이아웃

### 1. SDS_TYPE_8 예시 (작은 문자열)

메모리 레이아웃:
┌─────────┬─────────┬─────────┬──────────────┬─────┐
│  len    │  alloc  │  flags  │  "hello"     │ \0  │
│ (1byte) │ (1byte) │ (1byte) │  (5bytes)    │(1byte)
└─────────┴─────────┴─────────┴──────────────┴─────┘
↑                                    ↑
헤더 시작                          실제 문자열 포인터 (sds)

~~~
struct sdshdr8 {
    uint8_t len;        // 현재 사용 중인 길이
    uint8_t alloc;      // 할당된 총 공간 (null terminator 제외)
    unsigned char flags; // 타입 정보 (하위 3비트)
    char buf[];         // 실제 문자열 데이터
};

// sds는 실제로는 문자열 버퍼의 시작 포인터
typedef char *sds;

// 하지만 헤더는 그 앞에 있습니다!
// 헤더를 가져오는 매크로:
#define SDS_HDR(8,s) ((struct sdshdr8 *)((s)-(sizeof(struct sdshdr8))))

// 예시
sds mystring = sdsnew("hello");
// mystring은 "hello"의 시작 주소를 가리킴
// 하지만 실제 할당된 메모리는:
// [헤더 3바이트][hello\0]
//              ↑
//         mystring이 가리키는 곳
~~~
메모리 효율성 분석
- 일반 C 문자열:
  - char* 포인터: 8바이트 (64비트)
  - 문자열: 6바이트 ("hello\0")
  - 총: 14바이트 (포인터 포함)
- SDS_TYPE_8:
  - 헤더: 3바이트 (len + alloc + flags)
  - 문자열: 6바이트 ("hello\0")
  - 총: 9바이트
  - 절약: 약 35% 메모리 절약

~~~
struct sdshdr5 {
unsigned char flags; // 하위 3비트: 타입, 상위 5비트: 길이
char buf[];
};
~~~

- "hi" (2바이트)
  - SDS_TYPE_5 사용 가능:
  - 헤더: 1바이트 (flags에 길이 인코딩)
  - 문자열: 3바이트 ("hi\0")
  - 총: 4바이트


### SDS 장점 

~~~
// 일반 C 문자열 - 길이 확인
size_t len = strlen(str);  // O(n) - 문자열 전체를 읽어야 함

// SDS - 길이 확인  
size_t len = sdslen(str);   // O(1) - 헤더에서 바로 읽음
~~~

~~~
struct sdshdr {
    int len;     // 문자열의 길이
    int free;    // 할당된 버퍼 중 사용되지 않은 공간의 길이
    char buf[];  // 실제 문자열 데이터를 저장하는 버퍼
};
~~~

1. len: 현재 문자열의 길이, 이는 strlen 함수를 호출하지 않고도 문자열의 길이를 빠르게 알 수 있게 함
2. free: 현재 할당된 버퍼 중 사용되지 않은 공간의 길이. 이를 통해 문자열을 확장할 때 메모리 재할당을 최소화
3. buf: 실제 문자열 데이터를 저장하는 버퍼. 맨 마지막엔 Null(1byte)로 끝남. 이 버퍼는 NULL로 종료되는 C 문자열과 호환