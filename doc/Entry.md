# Entry 객체 (Entry Object)

- Entry = 필드-값 쌍을 하나의 메모리 블록에 패킹한 객체
- 현재는 Hash 타입에서만 사용
- 선택적으로 만료 시간(expiration) 메타데이터 포함 가능

사용 예시
~~~
HSET user:1000 name "John" age "30"
~~~

~~~
Hash "user:1000":
  - Entry 1: field="name", value="John"
  - Entry 2: field="age", value="30"
~~~