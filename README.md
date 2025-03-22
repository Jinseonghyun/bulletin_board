## 서비스 단계에 따라 적재적소에 DB를 활용하는 게시판 프로젝트

### 1. MySQL을 활용하여 가장 기본적인 게시판 기능 개발

#### 초기 게시판 MVP 개발
- 유저 인증/권한
- 게시글 CRUD
- 댓글 CRUD
- 대댓글

서버 다운 위기 1 : 다양한 검색 조건으로 인해 index가 많아질 시 INSERT, UPDATE 쿼리 속도가 저하되고 DB에 과도한 부하 발생

---

### 2.  Elasticsearch 도입 및 sharding으로 부하분산 해결

#### 물품 검색 결과 1초 구현
- 게시글/댓글 검색 기능 
- 게시글/댓글 모니터링 대시보드

서버 다운 위기 2 : 다량 데이터가 빈번하게 mySQL에서 조회되면서 mySQL에 부하 집중되고 transaction lock이 발생

---

### 3. 싱글스레드 Redis에 캐시로 저장

#### 물품 검색 결과 1초 구현
- 인기글 캐시
- 광고 캐시

서버 다운 위기 3 : 유저 및 데이터가 늘면서 MySQL에 적재하기 부담스러워지고 테이블 수정 필요 시 Online DDL로도 변경이 불가능함

---

### 4. MongoDB 도입으로 동적인 스키마(NoSQL)를 활용하여 다양한 데이터 수집

#### 다양한 데이터 로그로 저장
- 광고 노출 로그
- 광고 클릭 로그

서버 다운 위기 4 : 10만명의 유저가 동시에 서비스에 접속한다면 서버나 DB들이 auto-scaling되기 전에 대량의 트래픽을 버티지 못함

---

### 5. RabbitMQ 도입으로 비동기 방식 메시지 브로커 처리 도입

#### 활동 알림 푸시 구현
- 공지사항
- 광고 푸시 알림


### 프레임워크 및 기술 스택
- JAVA, Spring
- Spring Boot
- Spring Data JPA
- SPring Security
- Spring Batch
- JWT
- Gradle
- Swagger

### DB 및 데이터 스트리밍 플랫폼
- Redis
- MySQL
- MongoDB
- RabbitMQ
- Elasticearch

### 모니터링용 대시보드
- Logstash
- Kibana

### 테스트 및 DevOps
- AWS EC2
- Docker
- Locust

