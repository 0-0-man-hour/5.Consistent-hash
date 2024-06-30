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
#### 사전 준비
서버로 사용되는 mongodb와 redis의 사용을 위해서 먼저 docker의 설치가 필요하다.  
- [docker 다운로드](https://www.docker.com/products/docker-desktop/)
- shell에 명령어 입력 : docker pull mongo, docker pull mongo


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

위 yml 파일에서 필요한 설정을 한 뒤에 application을 실행하여 사용할 수 있다.
- function: md5, custom
- consistent: true(안정해시 사용), false(모듈러 해싱 사용)
- node-nums: virtual node의 수를 선택
- infra: mongo(가상 서버 개념), redis(실제 서버 사용)



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

해당 project를 jar로 빌드한 뒤 --- file을 경로에 저장한다.
이후에 build.gradle에서 dependency에 다음과 같이 추가한뒤 library를 불러온다.

``` gradle
dependencies {
    implementation files('libs/consistenthash-0.0.1-SNAPSHOT-plain.jar')
}
```
이후 jar안의 빈을 사용하기 위해서 config class를 생성하여 프로젝트 안의 bean을 scan하여 사용한다.  

``` java
@Configuration
@ComponentScan(basePackages = "com.zeromh.consistenthash")
public class ConsistentConfig {
}
```


다음엔 consistentHashService를 불러온 뒤 각 메소드를 호출한다.

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
자세한 테스트 결과는 [5. 안정해시 테스트 결과](https://0manhour.notion.site/5-f140c258a5e94093bfa9f5953de168d8?pvs=4)에서 확인할 수 있다.  

- 키 분포 확인(Modular, Consistent hash - virtual node 수 별)
- 서버 down 시 Cache Hit Rate 비교
- Server 추가/제거 시 Key 이동 시간 비교



