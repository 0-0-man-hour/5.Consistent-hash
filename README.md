# 5. Consistent-hash

5장 안정해시에 대한 구현 과제입니다.  
notion: https://0manhour.notion.site/5-f140c258a5e94093bfa9f5953de168d8?pvs=4  
담당자: 박상엽(park-sy)  

|Week|Date|Desc|
|------|---|---|
|1주차|24.05.19~|내용 정리 및 설계|
|2주차|24.05.26~|안정 해시에 대한 기본 로직 구현 및 테스트|
|3주차|24.06.02~|안정 해시에 대한 실제 컨테이너에서의 key 이동 구현 및 테스트|
|4주차|24.06.09~|서비스 라이브러리화 및 문서 정리|


### 상세 설계  
```bash.
└── com
    └── zeromh
        └── consistenthash
            ├── ConsistenthashApplication.java
            ├── application
            │   ├── KeyManageUseCase.java
            │   ├── ServerManageUseCase.java
            │   ├── dto
            │   │   ├── KeyServerDto.java
            │   │   ├── ServerStatus.java
            │   │   └── ServerUpdateInfo.java
            │   └── impl
            │       ├── KeyManageService.java
            │       └── ServerManageService.java
            ├── domain
            │   ├── model
            │   │   ├── hash
            │   │   │   ├── CustomHashFunction.java
            │   │   │   ├── HashFunction.java
            │   │   │   └── MD5HashFunction.java
            │   │   ├── key
            │   │   │   └── HashKey.java
            │   │   └── server
            │   │       ├── HashServer.java
            │   │       ├── ServerPort.java
            │   │       ├── mongo
            │   │       │   └── MongoServerAdapter.java
            │   │       └── redis
            │   │           ├── DockerService.java
            │   │           └── RedisServerAdapter.java
            │   └── service
            │       └── hash
            │           ├── HashServicePort.java
            │           └── impl
            │               ├── ConsistentHashAdapter.java
            │               └── ModularHashAdapter.java
            ├── interfaces
            │   ├── key
            │   │   ├── HashKeyRequestDto.java
            │   │   └── KeyController.java
            │   └── server
            │       ├── ServerController.java
            │       └── ServerRequestDto.java
            └── util
                └── DateUtil.java
```
해당 구조는 헥사고날 아키텍처를 기반으로 설계했으며 [지속 가능한 소프트웨어 설계 패턴: 포트와 어댑터 아키텍처 적용하기](https://engineering.linecorp.com/ko/blog/port-and-adapter-architecture)와 클린 아키텍처 도서를 참고하였다.  
![image](https://github.com/0-0-man-hour/5.Consistent-hash/assets/53611554/93bc8a21-dbc8-4709-9f0a-dda495d4e3fb)


- API 또는 내부 컴포넌트를 호출하여 서버/키 관리 serveice에 접근하여, 서버와 키를 추가/제거 할 수 있다.
- 논리적인 흐름을 보기 위하여 mongodb를 활용하여 서버의 상태 저장, key 배치 등을 확인한다.
- 물리적으로는 redis를 사용하여 서버 추가/제거 시 redis container를 docker를 활용하여 띄우고 key를 각 container에 저장하도록 한다.
- 서버의 변경, 해시 알고리즘 변경, virtual node의 변경 등은 코드 수정 없이 application.yml에서 값을 조정하여 변경할 수 있다.
``` yml
hash:
  function: md5 #custom
  consistent: true #false
  node-nums: 100 #1, 4....

server:
  infra: redis # mongo
  host: localhost

```

### 주요 기능
- 안정 해시를 통한 서버/키 설정
``` java
public HashServer getServer(HashKey key) {
    if (ring.isEmpty()) {
        return null;
    }

    long hash = hashFunction.hash(key.getKey());
    key.setHashVal(hash);

    if (!ring.containsKey(hash)) {
        SortedMap<Long, HashServer> tailMap = ring.tailMap(hash);
        hash = tailMap.isEmpty() ? ring.firstKey() : tailMap.firstKey();
    }
    return ring.get(hash);
}
```
안정 해시를 구현하기 위해서 SortedMap을 사용하였으며, hash value를 기반으로 정렬된 형태의 map을 구성하였다.  
서버의 상태를 map에 저장하고, 조회 시에 tailMap을 통해 현재 value로 부터 가장 가까운 위치의 서버를 반환한다.  
[Consistent Hashing](https://tom-e-white.com/2007/11/consistent-hashing.html) 사이트를 참고하여 구현하였다. 

- 서버 추가  

서버의 추가는 server를 해시링에 등록하고, 외부 서버를 생성하여, key를 저장할 수 있도록 한다.
1. 서버의 추가는 server의 이름을 해시 알고리즘을 통해 hashvalue를 계산한 후에 해시링에 배치한다.
2. 배치된 해시링을 내부 map에 저장 한 후에 ServerPort를 거쳐 상태를 등록한다.  
    2-1. mongodb의 경우 논리적인 서버 상태가 저장된다.  
   ![mongo](https://github.com/0-0-man-hour/5.Consistent-hash/assets/53611554/84abbd08-a6fc-440c-bf73-dd3cffeced39)

    2-2. redis의 경우 server 이름을 기반으로한 컨테이너가 생성된다.
    ![redis](https://github.com/0-0-man-hour/5.Consistent-hash/assets/53611554/65f208d9-99ae-4c7a-b13d-ffea1132fe8b)
3. rehash를 체크한 경우에 기존에 저장된 key들의 rehash가 이루어진다.
  
- 서버 제거

서버의 제거는 해시링에 등록된 server 정보를 제거하고, 외부 서버를 제거한다.
1. 서버의 제거는 해시링에 배치된 서버의 데이터를 제거한다.
2. 배치된 해시링을 내부 map에서 작제한 후에 ServerPort를 서버 정보를 삭제한다.  
    2-1. mongodb의 경우 논리적인 서버 상태를 삭제한다.  
    2-2. redis의 경우 server 이름 기반의 컨테이너를 제거한다.
3. rehash를 체크한 경우 삭제 대상의 서버의 key를 rehahs하여 재배치한다.
  
- 키 추가/제거

키 추가는 키의 hash value를 통해서 가장 가까운 위치의 서버를 찾고, 그 서버에 데이터를 저장/제거한다.
1. key의 hash value를 계산하여 getServer() 메소드를 호출한다.
2. 해당 서버의 정보를 반환 받고, serverPort를 통해 데이터의 저장/삭제를 호출한다.


### 사용방법
#### 서버 구동 방법
``` yml
hash:
  function: md5 #custom
  consistent: true #false
  node-nums: 100 #1, 4....

server:
  infra: redis # mongo
  host: localhost

```

위 yml 파일에서 정보를 바꾸어 실행하여 사용할 수 있다.
- function: md5, custom
- consistent: true(안정해시 사용), false(모듈러 해싱 사용)
- node-nums: virtual node의 수를 선택
- infra: mongo(가상 서버 개념), redis(실제 서버 사용)

*redis 사용 시 docker 설치가 필요*

#### API 사용
API를 통한 사용은 서버를 실행한 후 아래 API를 호출하여 사용한다.

|method|path|desc|
|------|---|---|
|POST|/consistenthash/server|server를 추가한다.|
|DELETE|/consistenthash/server|server 제거한다.|
|GET|/consistenthash/key/{key}|key를 조회한다.|
|POST|/consistenthash/key|key를 추가한다.|
|DELETE|/consistenthash/key|key를 제거한다.|


#### 컴포넌트를 통한 사용(6장. 키값 저장소 설계 이후에 작성 예정입니다.)
.jar 파일을 불러와 사용할 수 있다.
consistentHashService를 불러온 뒤 각 메소드를 호출한다.

|method|desc|
|------|---|
|ConsistentHashService.addServer()|server를 추가한다.|
|ConsistentHashService.delServer()|server 제거한다.|
|ConsistentHashService.getKey()|key를 조회한다.|
|ConsistentHashService.addKey()|key를 추가한다.|
|ConsistentHashService.delKey()|key를 제거한다.|

``` java
@Service
@RequiredArgsConstructor
public class TestService {
    private final ConsistentHashService hashService;
    
    public void test() {
        hashService.addServer("myServer");
        hashService.delServer("myServer");
    }
}
```


### 테스트
자세한 테스트 결과는 [5. 안정해시 테스트 결과](https://docs.google.com/spreadsheets/d/19TCaYxdEXc0qGslYcfGyjHuE0nQaXxMYe5e_cXF5M88/edit?usp=sharing)에서 확인할 수 있다.  

#### Test Set
- 테스트 서버의 기본값: 4개  
    - server의 이름은 server_{server.no}와 같으며,  virtual node의 수에 따라서 server_{server.no}{node.no}에 해시 알고리즘을 적용하여 해시값을 생성하였다.
    - ex)"server_00", "server_01", "server_10", "server_11"  
- key의 개수: 100만개
    - key 값은 0부터 999999까지의 숫자를 String 형태로 바꾸어 사용하였다.
    - ex) "0", "1", "2", ... ,"999999"
- 해시 알고리즘: MD5
    - MD5는 총 16byte이므로 간단하게 사용하기 위해 prefix 부터 4byte까지 잘라 사용하였다.
- 안정 해시의 virutal node 수: 4개의 case  
    - 1, 4, 10, 100개  

위 테스트 설정을 토대로 안정 해시에 server를 생성하고, hash value를 적용하여 해시링에 위치하면 다음과 같다.

<img src="https://github.com/0-0-man-hour/5.Consistent-hash/assets/53611554/4d016a63-b8c2-48ec-ae09-9e942ec8b0bc" width="500"/>
<img src="https://github.com/0-0-man-hour/5.Consistent-hash/assets/53611554/0bba574e-be1f-402d-a2cb-ed092bde565b" width="500"/>
<img src="https://github.com/0-0-man-hour/5.Consistent-hash/assets/53611554/2be3e8fc-1524-457e-96af-7b81d6840e75" width="500"/>
<img src="https://github.com/0-0-man-hour/5.Consistent-hash/assets/53611554/6e1f3492-d11f-4e98-b5d1-92a8529f4a40" width="500"/>


#### 키 분포 확인
키 분포 확인을 위해 100만개의 key의 해시값을 계산한 후에 각각의 위치에 배치하였다.  
모듈러 해싱의 경우, hash value % 4(서버의 수)를 통해 각 서버로 배치  
안정 해싱의 경우, 각 서버의 hash value를 해시링에 위치 시킨후 key의 hash value가 가장 가까운 서버로 향하도록 하여 배치하였다.  
![스크린샷 2024-06-09 오후 9 24 55](https://github.com/0-0-man-hour/5.Consistent-hash/assets/53611554/6a243c77-0490-437e-9204-fa098993a5bc)

각 해싱 알고리즘과 virtual node 수에 따른 키 분배의 결과는 위와 같다.  
- 모듈러 해싱의 경우, 모든 hash value에 대해 모듈러 연산을 통해 값을 계산하기 때문에 key의 개수가 충분하다면, 고르게 분포되는 것을 예측할 수 있고, 결과 또한 4개의 서버가 약 25만개의 key를 나눠가진 것을 확인할 수 있다.  

- 반면 안정 해싱의 경우에는 상대적으로 key의 분포가 고르지 못한 것을 확인할 수 있는데, 특히 virtual node 수가 1일 때는 0번 서버의 key가 매우 적은 것을 확인할 수 있다.  
    - 0번 서버의 hash value(3,208,578,106)가 1번 서버의 hash value(3,172,837,842)와 매우 밀접해 있기 때문이다.
    - 이로 인해 0번 서버는 (3,172,837,843 ~ 3,208,578,106, 35,740,264)의 hash value를 가진 key만 가져올 수 있기 때문이다.
    - 이 비율은 전체 hash value가 4btye에서 나올 수 있는 4,294,967,295개 이므로 35,740,264/4,294,967,295 ~= 0.83%만을 차지한다.  
- 이 것은 안정 해시의 단점을 보여주는 것인데, 이를 해결하기 위해서 virtual node의 개념을 도입하였고, node의 개수가 증가할 수록 key가 고르게 분포하는 것을 확인할 수 있다.  


#### 서버 down 시 Cache Hit Rate  
키 분포 확인 이후에 하나의 서버가 down 되었다 가정하고 cahce hit rate를 테스트하였다.  
안정 해시/virtual node 1 일 때 key의 개수가 유독 적어 Server_3이 down 되었다 가정하였고, 나머지는 Server_0의 down으로 테스트하였다.  
![스크린샷 2024-06-09 오후 9 41 03](https://github.com/0-0-man-hour/5.Consistent-hash/assets/53611554/1d2a9b6e-6b19-49e7-8ab0-d73755df1a11)

특정 서버 down 시의 cahce hit rate의 변화는 위와 같다.  
- 모듈러 해싱의 경우 cahce miss의발생이 매우 증가하였다.
    - 모든 key의 나머지 연산값이 변동되므로(hash value % 4 -> hash value % 3) 모든 key의 배치가 변화되었기 때문이다.
    - 또한 각 서버들의 저장되어 있는 key 또한 
- 반면 안정 해싱의 경우 virtual node의 수가 1일 경우를 제외하고는 서버 down에 대하여 안정적인 cahce hit rate을 유지한다.
    - 안정 해시의 특성으로, 기존 서버가 가지고 있는 key는 계속 유지되기 때문이다. 이는 기존 key의 개수 cahce hit 수가 같다는 것에서 확인할 수 있다.
    - down된 서버가 가지고 있던 key에 대해서만 cache miss가 발생하였고, 그 값의 크기가 cache hit rate의 가장 큰 변수이다.
- virtual node 수가 1일 경우에는 Server_0,1은 cache hit rate 100%를 유지하며, down된 Server_3의 요청이 해시링에서 다음 서버인 Server_2로 간 것을 확인할 수 있다.  


#### Server 추가/제거 시 Key 이동 시간 비교  
책에서는 다루지 않은 내용이지만, Server가 추가/제거될 때 데이터의 보존이 중요할 경우 Rehash 과정을 거쳐 알맞은 위치로 key를 이동시켜야 한다.  
이 과정에서 key 이동 시간에는 서비스를 중단시켜야 하므로, 서비스의 만족도의 큰 영향을 미칠 수 있기 때문에 각 분배 해싱의 key 이동 시간을 비교해보았다.  

![스크린샷 2024-06-09 오후 9 48 20](https://github.com/0-0-man-hour/5.Consistent-hash/assets/53611554/ee71c6c6-e46e-4811-99e0-7e51fc220cd1)

논리적 서버인 mongodb와 물리적 서버인 redis로 테스트 한 결과는 위와 같으며, local에서 container를 사용하여 측정하였기 때문에, 실제 값이 얼마인지 보다는 상대적으로 소요된 숫자를 비교하였다. 
- 모듈러 해싱의 경우 Server 추가/제거에 대해서 모두 높은 시간을 보인다.  
- 안정 해싱의 경우 virtual node 수가 1일 때를 제외하고, Server 추가는 모듈러 해싱과 비슷하게, Server 제거는 모듈러 해싱에 비해 줄어든 모습을 보인다.  

각 시간이 차이나는 원인은 rehash 과정을 거치는 키의 개수와 관련이 있다.  
![스크린샷 2024-06-09 오후 9 50 31](https://github.com/0-0-man-hour/5.Consistent-hash/assets/53611554/7c4b5de8-a48d-409f-81c0-45b0f7096c13)  
위 표를 살펴보면 Server_4가 추가되기 전후로 key의 증감을 확인할 수 있는데, 모듈러 해싱은 키의 증감과 상관없이 1M 개의 데이터를 모두 rehash 해야하기 때문에 증가/제거 모두 오랜 시간이 소요된다.  
반면 안정 해싱의 경우엔 추가의 rehash 대상이 되는 Server에 대해서만 key를 rehash하면 되므로 제거 시에는 Server_4에 대해서만 진행하게 되고, virtual node 수가 1일 경우에는 증가 시에도 Server_3에 대해서만 진행하면 되기 때문에 시간이 적게 소요된다.  

### 특이사항
테스트 결과에서 virtual node를 추가하는 것이 항상 좋은 결과를 나오게 하지만, 항상 그렇지만은 않다.  
hash value를 통해서 특정 서버를 찾는 과정은 기본적으로 binary search에 기반하는데, 이 때의 시간 복잡도는 O(logN)이다.(모듈러 해싱은 O(1)이 소요된다.)  
여기서 N은 (서버의 개수 x virtual node의 수)이며 virtual node의 수가 증가할 수록 key의 서버를 찾는데 걸리는 시간이 늘어난다는 것을 의미한다.  

따라서 virtual node의 수를 선택할 때에는, 개수에 key의 분포 및 cache hit rate와 조회 속도의 trade-off를 고려하여 설정해야 한다.

