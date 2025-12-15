📌 알림 발송 시스템 과제
## 1. 지원자 정보
- **지원자 ID**: 2632-000260
- **이름**: 박지현
- **지원 포지션**: 서버 개발자 - 뱅킹
---
## 2. 프로젝트 실행 방법
```bash
 cd subprojects/infra
 docker compose up
  # 이후 서버 실행
```

## 3. 프로젝트 개요
본 프로젝트는 **알림 발송을 등록하고 조회하는 알림 API 서버**를 구현하는 과제입니다.
알림 서비스의 **성능과 확장성**을 고려하여 **Kafka 기반 비동기 처리 구조**로 설계하였으며,
발송 실패 건에 대해서는 **재시도 및 중복 방지 로직**을 포함합니다.

#### 주요 특징
- 알림 발송 요청을 **비동기 처리**하여 API 응답 지연 최소화
- 실패한 알림에 대한 **재시도 배치 처리**
- **eventId 기반 멱등성 보장**으로 중복 발송 방지
- 예약 알림 및 대량 알림 처리 고려

---
## 4.아키텍처 설계
#### 전체 흐름
![Architecture](./kakaobank-task-system.png)
#### 알림 등록 API
- 알림 요청을 DB에 PENDING 상태로 저장
- Kafka에 이벤트 발행
- 즉시 ``202 Accepted`` 응답 반환

#### 알림 조회 API
- 발송 성공/실패 여부와 관계없이
- 요청 직후부터 DB 기반 조회 가능

#### Kafka Consumer
- Kafka 메시지를 소비하여 알림 발송 처리
- 발송 성공 → ``SUCCESS``
- 발송 실패 → 최대 3회 retry 후 ``FAILED``

#### 재시도 배치
- ``FAILED`` 상태 알림을 30분마다 재시도
- retryCount가 10회를 초과하면 ``DEAD`` 상태로 전환
(총 최대 30회 재시도)

#### 예약 알림 처리
- ``RESERVED`` 상태이며 예약 시간이 도래한 알림을
- 1분 주기의 배치 잡에서 처리
Failed Scheduler와 Reserved Scheduler 모두 Kafka로 이벤트를 발행하도록 구성하여
코드 중복을 줄이고 모듈 간 결합도를 낮췄습니다.

## 5. 상세 설계

🔹 **Kafka 활용**
- 비동기 처리를 위해 Kafka 사용
- Partition Key
    ```
    channel:target
    ```
    → 사용자 + 채널 단위로 발송 순서 보장
- ``concurrency = 3`` 설정으로 병렬 처리 성능 확보

🔹 **중복 발송 방지 (Idempotency)**
- Kafka 메시지 발행 시 UUID 기반 ``eventId`` 생성
- 동일 ``eventId``가 이미 ``SUCCESS`` 상태인 경우 처리 제외
- Scheduler 간 동시성 문제 방지를 위해 
  - Consumer에서 메시지 수신 직후
  - 새 트랜잭션을 열어 ``PENDING`` 상태로 즉시 DB 기록
  - 다른 스레드에서 동일 row를 처리하지 못하도록 차단

🔹 **조회 API**
- 다양한 검색 조건을 유연하게 처리하기 위해 QueryDSL 사용
- 사용자, 기간, 상태, 수신자, 채널 조건 기반 조회 지원

🔹 **확장성과 안정성 고려**
- 서버 세션 / 로컬 캐시 사용 지양 → Stateless 구조
- 알림 발송 실패 시 이벤트 유실 방지를 위해 비휘발성 DB 기반 저장
- 채널 확장을 고려해 Channel을 Enum으로 관리

🔹 **DB 인덱스 설계**
- ``(sendAt, createdAt)`` 복합 인덱스
- ``event_id`` 유니크 키

6. 프로젝트 구조
```
   └── subprojects
   ├── application
   │   ├── notification-kafka-consumer
   │   ├── scheduler
   │   └── service
   ├── client
   │   └── notification-sender
   ├── domain
   ├── infra
   └── ui
   └── api
```

멀티 모듈 구조로 구성하였으며
**Layered Architecture (ui → application → domain / infra / client)** 를 따릅니다.
각 모듈은 인터페이스 기반 설계로 확장성과 테스트 용이성을 고려했습니다.

7. API 문서

Swagger UI: http://localhost:8080/swagger-ui.html

8. Kafka UI

http://localhost:6067

9. H2 Console

http://localhost:8080/h2-console