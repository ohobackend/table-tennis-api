# Table Tennis API

Java 17 · Spring Boot 3.5.5 · Gradle Kotlin DSL 8.14.3 · Spring Data JPA · PostgreSQL 15 · Flyway · Spring Security JWT · MapStruct · springdoc-openapi.

## 실행

Docker Desktop의 Linux 컨테이너 엔진을 실행한 후:

```powershell
cd backend
Copy-Item .env.example .env
docker compose up -d --build --wait
docker compose ps
```

- API: http://localhost:8080/api/v1
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs
- 상태: http://localhost:8080/actuator/health
- 종료: `docker compose down` — DB 볼륨은 보존됩니다.
- 포트 충돌 시 `.env`의 `API_PORT`, `DB_PORT`를 변경합니다.
- 운영에서는 `JWT_SECRET`(UTF-8 32바이트 이상), `POSTGRES_PASSWORD`, `CORS_ALLOWED_ORIGINS`를 배포 환경에 맞게 설정합니다. origin은 쉼표로 구분합니다.
- Compose 기본 비밀값과 DB 비밀번호는 로컬 개발용입니다.

로컬 JDK로 실행하려면 **JDK 17**을 설치하고 JAVA_HOME을 지정합니다.

```powershell
docker compose up -d postgres
.\gradlew.bat bootRun --args='--spring.profiles.active=dev'
```

Linux/macOS에서는 `./gradlew`를 사용합니다. Java 26은 이 프로젝트의 실행 JDK로 사용하지 않습니다.

## 테스트

실제 PostgreSQL에서 Flyway 적용, Hibernate 검증, MockMvc HTTP 요청, 인증·권한·CRUD·집계·점수 동시성을 확인합니다.

Docker만 설치된 환경:

```powershell
.\scripts\test.ps1
```

또는 직접:

```sh
docker compose -p tt-api-tests -f docker-compose.test.yml run --rm tests
docker compose -p tt-api-tests -f docker-compose.test.yml down
```

테스트 DB는 서비스용 DB와 분리되어 있고 tmpfs를 사용합니다. 테스트 컨테이너를 종료하면 DB가 정리됩니다. Gradle 캐시만 별도 볼륨에 유지합니다.

JDK 17과 Docker를 사용할 수 있다면 `./gradlew test`도 가능합니다. `TEST_DATABASE_URL`이 없으면 Testcontainers가 PostgreSQL 15를 시작합니다. 이 환경변수를 지정할 경우 **테스트 전용 DB**와 `TEST_DATABASE_USERNAME`, `TEST_DATABASE_PASSWORD`를 지정해야 합니다.

테스트 결과: `build/reports/tests/test/index.html`

## 회원 및 관리자

회원가입:

```json
POST /api/v1/auth/signup
{
  "email": "admin@example.com",
  "password": "password123!",
  "userName": "관리자",
  "realName": "관리자"
}
```

가입은 항상 USER입니다. 첫 관리자는 DB 관리자가 명시적으로 승격합니다.

```sh
docker compose exec postgres psql -U tt_user -d table_tennis
```

psql 안에서:

```sql
UPDATE "user" SET role = 'ADMIN' WHERE email = 'admin@example.com';
```

이후 로그인합니다:

```json
POST /api/v1/auth/login
{"email":"admin@example.com","password":"password123!"}
```

응답의 `data.accessToken`을 Swagger의 Authorize 또는 `Authorization: Bearer ...` 헤더에 사용합니다.

- Access JWT: 1시간. HS256, issuer/만료/세션/현재 권한 검증.
- Refresh Token: 14일. 난수 토큰이며 서버에는 SHA-256 해시만 저장합니다.
- `POST /auth/refresh`: `{"refreshToken":"..."}`. 매번 회전하며 이전 토큰은 재사용할 수 없습니다. 14일의 원래 세션 만료는 연장하지 않습니다.
- `POST /auth/logout`: Access Token 필요. 해당 로그인 세션의 Access/Refresh 모두 즉시 무효화합니다.
- 프로필 공개 응답은 비밀번호, 이메일, 전화번호, 생년월일을 제외합니다.
- `PUT /users/{id}`: 본인 또는 ADMIN. 권한·비밀번호·이메일은 이 API로 수정하지 않습니다.
- 댓글 작성자 ID와 표시명은 인증 회원으로 결정합니다.

## 대회 → 경기 → 결과 사용 순서

1. 회원가입 또는 선수 목록 조회로 userId 확보.
2. ADMIN이 `POST /tournaments`로 대회 생성.
3. `POST /tournaments/{year}/{id}/participants`에 `{"userId":1}` 등록.
4. `POST /tournaments/{year}/{id}/competitions`로 경기 단계 생성.
5. hasGroups=Y이면 조 생성 및 조별 참가자 등록.
6. `POST /matches`로 대진 등록.
7. `POST /matches/{id}/sets`로 점수 입력. 기본 3선승 충족 시 자동 완료.
8. `/results`, `/rankings`, `/players/{id}/stats`에서 결과 조회.

경기 단계 생성 예:

```json
{
  "competitionName": "예선",
  "competitionType": "ROUND_ROBIN",
  "matchFormat": "SINGLES",
  "competitionOrder": 1,
  "hasGroups": "N",
  "status": "SCHEDULED"
}
```

대회 생성은 명세서의 요청 예시를 따릅니다. `tournamentYear`, `tournamentName`, `startDate`, `endDate`는 필수입니다.

경기 생성 예:

```json
{
  "competitionId": 1,
  "matchRound": 0,
  "courtNumber": 1,
  "scheduledAt": "2026-09-05T14:00:00+09:00",
  "location": "포항체육관",
  "participants": [
    {"userId": 1, "side": "SIDE_A", "participantOrder": 1},
    {"userId": 2, "side": "SIDE_B", "participantOrder": 1}
  ]
}
```

댓글: `{"commentContent":"내용","commentDepth":0}`

조 생성·수정: `{"groupName":"A조"}`

대회 참가자 수정: `{"userId":1,"finalRank":1,"notes":"비고"}`

조 참가자 등록: `{"userId":1,"groupRank":1}`, 순위 수정: `{"groupRank":1}`

세트 등록:

```json
{
  "sets": [
    {"setNumber":1,"sideAPoint":11,"sideBPoint":8},
    {"setNumber":2,"sideAPoint":12,"sideBPoint":10},
    {"setNumber":3,"sideAPoint":11,"sideBPoint":5}
  ]
}
```

세트 수정은 배치 래퍼 없이 `{"setNumber":1,"sideAPoint":11,"sideBPoint":7}` 형태입니다.

모든 URL 앞에 `/api/v1`을 붙입니다. 전체 엔드포인트 및 DTO 필드는 Swagger에서 확인할 수 있습니다.

## 응답·검색

```json
{"success":true,"data":[],"meta":{"page":1,"size":20,"total":0}}
```

```json
{"success":false,"error":{"code":"VALIDATION_ERROR","message":"요청 값 검증 실패"}}
```

- 목록은 1부터 시작하는 `page`, `size`(1~100), `sort=reg_date,desc`를 지원합니다. 날짜 성격이 없는 목록은 ID 또는 진행 순서를 기본 정렬로 사용합니다.
- 추가 정렬은 해당 엔티티의 허용된 camelCase 필드명입니다. 예: notices의 `noticeTitle,asc`, matches의 `scheduledAt,asc`.
- 검색은 파라미터 바인딩을 사용하고 LIKE 와일드카드를 이스케이프합니다.
- 랭킹은 승률 내림차순 → 경기수 내림차순 → userId 오름차순으로 고정 정렬합니다.
- 세트 목록은 최대 5개로 세트 번호순이며 페이지네이션하지 않습니다.
- 일자는 YYYY-MM-DD, 경기 시각은 ISO 8601입니다. 오늘·주·월·연도 경계는 Asia/Seoul입니다.

## 명세 보완 및 범위

- `user`, `group`, `match`는 PostgreSQL 인용 식별자와 JPA 매핑을 사용합니다.
- 복합키 tournament(year,id)는 시퀀스로 순번을 발급합니다. group_participant는 (groupId,userId), 선택 통계 테이블 match_stats는 (matchId,userId)를 PK로 사용합니다.
- 명세의 경기 날짜/장소 및 기간 랭킹을 위해 `match.scheduled_at`, `location`, `completed_at`을 추가했습니다. 동시 변경을 위한 `version`도 추가했습니다.
- 인증 세션은 V2 마이그레이션의 `auth_session`에 보관합니다.
- 명세에 생성 경로가 빠진 competition은 별도 CRUD를 제공하여 전체 흐름을 API만으로 진행할 수 있습니다.
- 기존 2025.06 레거시 스키마는 생성하지 않습니다.
- 단식은 양쪽 1명, 복식은 양쪽 2명, 단체전은 양쪽 3명 이상이며 같은 대회에 참가 등록되어야 합니다. 조가 지정된 경기는 해당 조에 등록된 선수만 허용합니다.
- 같은 경기 단계에서는 한 선수는 한 조에만 배정되고 조 정원을 검사합니다.
- 대회 참가 취소는 조·경기 배정이 없는 선수만 가능합니다. 점수가 등록된 경기의 대진은 변경할 수 없습니다.
- 대회 삭제는 소속 참가·경기 단계·조·경기·점수를 함께 삭제합니다. 게시글 삭제는 댓글을 함께 삭제합니다.
- 세트는 11점 이상 2점 차 종료, 1번부터 연속 입력, 승부 종료 후 추가 세트 금지입니다. 세트 번호 중복은 409입니다.
- `MATCH_SETS_TO_WIN`(application의 `match.sets-to-win`)은 2 또는 3, 기본 3입니다. 모든 경기 방식에 같은 값을 적용합니다.
- 완료된 세트의 점수 정정은 승자를 다시 계산합니다. 승리 조건이 사라지면 IN_PROGRESS로 돌아가고 completedAt을 비웁니다.
- 선택 기능인 다음 라운드 승자 자동 배정은 구현하지 않았습니다. nextMatchId 연결과 순환·소속 검증은 제공하며 다음 경기 대진은 ADMIN이 등록합니다.
- 랭킹은 COMPLETED 경기 기준입니다. 승률은 백분율(0~100), 평균득점은 경기당 자기 측 세트 점수 합계 평균입니다. 단체전·복식은 자기 측의 점수를 구성원마다 사용합니다.
- period는 all/week/month/year이며 현재 달력 기간의 시작부터 집계합니다. individual 결과는 단식·복식, team 결과는 단체전입니다.
- match_stats의 세부 에이스·실수 등은 선택 테이블만 생성하며 랭킹은 경기와 세트 데이터에서 직접 계산합니다.
- 관리자 변경 작업은 ADMIN_AUDIT 로거에 사용자 ID·메서드·경로·HTTP 상태를 기록합니다.

## 구조

```text
src/main/java/com/tabletennis/app/
  config/                  Security, JWT, CORS, Swagger, audit
  common/                  response, exception, util
  domain/
    auth/ user/ notice/ board/ comment/
    tournament/ participant/ competition/ group/
    match/ setscore/ ranking/ dashboard/
src/main/resources/db/migration/
  V1__init.sql
  V2__auth_sessions.sql
src/test/java/com/tabletennis/app/domain/
```

도메인 루트에 Entity/Repository/Service/Controller, 하위 dto 및 mapper에 요청·응답과 MapStruct 매퍼를 둡니다.

기술 호환성 참고: [Spring Boot 3.5 요구사항](https://docs.spring.io/spring-boot/3.5/system-requirements.html), [springdoc 호환표](https://springdoc.org/v2/faq.html).
