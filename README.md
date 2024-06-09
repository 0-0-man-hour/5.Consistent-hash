# 5. Consistent-hash

5장 안정해시에 대한 구현 과제입니다.  
notion: https://weak-delphinium-fcf.notion.site/5-f140c258a5e94093bfa9f5953de168d8?pvs=4

담당자: 박상엽(park-sy)  

|Week|Date|Desc|
|------|---|---|
|1주차|24.05.19~|내용 정리 및 설계|
|2주차|24.05.26~|안정 해시에 대한 기본 로직 구현 및 테스트|
|3주차|24.06.02~|안정 해시에 대한 실제 컨테이너에서의 key 이동 구현 및 테스트|


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
해당 구조는 헥사고날 아키텍처를 기반으로 설계했으며 [지속 가능한 소프트웨어 설계 패턴: 포트와 어댑터 아키텍처 적용하기](https://engineering.linecorp.com/ko/blog/port-and-adapter-architecture) 를 참고하였다.

//사진 추가 예정

### 주요 기능

설명 추가 예정입니다.


### API


### 사용방법
6장. 키값 저장소 설계 이후에 작성 예정입니다.

### 테스트
#### 키 분포 확인
![스크린샷 2024-06-09 오후 9 24 55](https://github.com/0-0-man-hour/5.Consistent-hash/assets/53611554/4cb455fe-90f0-4409-8256-090c39e35e8c)

모듈러 해싱을 통한 키 분배와 안정 해싱을 통한 키 분배의 결과는 위와 같다.  
특정한 해싱 결과를 토대로 키를 분배했기 때문에 replica 수가 적을 때는 고르지 못한 키의 개수를 확인할 수 있다.  
replica 수가 증가할수록 점점 고르게 분포한다.  
참고로 아래는 안정 해시의 해시링에 위치한 서버의 모습이다.  
<img src="https://github.com/0-0-man-hour/5.Consistent-hash/assets/53611554/9b829f29-160d-4c26-8a8d-6cc462dcbd21" width="500"/>
<img src="https://github.com/0-0-man-hour/5.Consistent-hash/assets/53611554/2e7561f4-aeb2-4168-967f-4cf83d95d8cd" width="500"/>
<img src="https://github.com/0-0-man-hour/5.Consistent-hash/assets/53611554/1bedf92f-831d-4998-a326-a465475a5ff7" width="500"/>
<img src="https://github.com/0-0-man-hour/5.Consistent-hash/assets/53611554/6ab82012-6547-463e-8a18-b8623a28d502" width="500"/>

#### 서버 down 시 Cache Hit Rate  
키 분포 확인 이후에 하나의 서버가 down 되었다 가정하고 cahce hit rate를 테스트하였다.  
안정 해시/replica 1 일 때 key의 개수가 유독 적어 Server_3이 down 되었다 가정하였고, 나머지는 Server_0의 down으로 테스트하였다.  
![스크린샷 2024-06-09 오후 9 41 03](https://github.com/0-0-man-hour/5.Consistent-hash/assets/53611554/1d2a9b6e-6b19-49e7-8ab0-d73755df1a11)

결과를 살펴보면 모듈러 해싱의 경우 모든 key의 나머지 연산값이 변동되므로(hash value % 4 -> hash value % 3) cahce miss가 많이 발생하였다.  
반면 안정 해싱의 경우 replica의 수가 1일 경우를 제외하고는 서버 down에 대하여 안정적인 cahce hit rate을 유지한다.  
replica 수가 1일 경우에는 Server_0,1은 cache hit rate 100%를 유지하며, down된 Server_3의 요청이 Server_2로 간 것을 확인할 수 있다.  


#### Server 추가/제거 시 Key 이동 시간 비교  
책에서는 다루지 않은 내용이지만, Server가 추가될 경우 Rehash 과정을 거쳐 알맞은 위치로 key를 이동시키는 것이 중요하다.  
이에 모듈러 해싱과 안정 해싱의 키 재이동 시간을 비교해보았다.  

![스크린샷 2024-06-09 오후 9 48 20](https://github.com/0-0-man-hour/5.Consistent-hash/assets/53611554/ee71c6c6-e46e-4811-99e0-7e51fc220cd1)

결과를 살펴보면, 상대적으로 모듈러 해싱의 경우 Server 추가/제거에 대해서 모두 높은 시간을 보이고 있고  
안정 해싱의 경우 replica 수가 1일 때를 제외하고, Server 추가는 모듈러 해싱과 비슷하게, Server 제거는 모듈러 해싱에 비해 줄어든 모습을 보인다.  

이 이유는 rehash 과정을 거치는 키의 개수와 관련이 있다.  
![스크린샷 2024-06-09 오후 9 50 31](https://github.com/0-0-man-hour/5.Consistent-hash/assets/53611554/7c4b5de8-a48d-409f-81c0-45b0f7096c13)
위 표를 살펴보면 Server_4가 추가되기 전후로 key의 증감을 확인할 수 있는데, 모듈러 해싱은 키의 증감과 상관없이 1M 개의 데이터를 모두 rehash 해야하기 때문에 증가/제거 모두 오랜 시간이 소요된다.  
반면 안정 해싱의 경우엔 추가의 rehash 대상이 되는 Server에 대해서만 key를 rehash하면 되므로 제거 시에는 Server_4에 대해서만 진행하게 되고, replica 수가 1일 경우에는 증가 시에도 Server_3에 대해서만 진행하면 되기 때문에 시간이 적게 소요된다.  

### 특이사항
