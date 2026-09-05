# 탁구 경기 결과 기록 관리 시스템 — 백엔드 API 개발 명세서

> 이 문서는 `2601_탁구경기기능설계.xlsx` (기능목록 / DB / 화면예시 / 사례 시트)를 근거로 작성되었습니다.
> Codex(AI 코딩 에이전트)에게 백엔드 구현을 지시할 때 이 문서를 그대로 프롬프트/컨텍스트로 사용하세요.

---

## 0. 문서 목적 및 사용법

- 이 문서는 **백엔드(REST API 서버) 구현 전용** 명세서입니다. 프론트엔드 명세는 `02_프론트엔드_개발명세서.md`를 참고하세요.
- Codex에게 지시할 때는 "이 문서의 N번 섹션을 기준으로 OOO 기능을 구현해줘" 형태로 섹션 단위 작업을 나누는 것을 권장합니다.
- 원본 엑셀에 기재되지 않은 세부 사항(인증 방식, 응답 포맷, 페이지네이션 등)은 **가정(Assumption)**으로 명시했습니다. 실제 구현 시 팀 컨벤션에 맞게 조정하세요.

---

## 1. 기술 스택 (가정)

엑셀 "개발 스택 예시"에 아래와 같이 후보가 제시되어 있습니다.

- DB: MySQL / PostgreSQL / Firebase Firestore
- 서버: Python(Flask, Django) / Node.js / Java Spring 등

**본 명세서는 다음 스택을 기준으로 작성합니다 (변경 가능):**

| 영역 | 선택 |
|---|---|
| 언어 | Java 17 |
| 프레임워크 | Spring Boot 3.x (Spring Web MVC) |
| 빌드 도구 | Gradle (Kotlin DSL) |
| ORM | Spring Data JPA + Hibernate |
| DB | PostgreSQL 15+ (MySQL 8로 대체 가능) |
| 인증 | Spring Security + JWT (Access Token + Refresh Token) |
| 문서화 | springdoc-openapi (Swagger UI) |
| 검증 | Jakarta Bean Validation (`@Valid`, `@NotNull` 등) |
| 매핑 | MapStruct (Entity ↔ DTO 변환) |
| 마이그레이션 | Flyway |
| 테스트 | JUnit5 + MockMvc + Testcontainers |

> Codex 지시 예시: "위 기술 스택으로 Spring Boot 3 + Gradle + JPA + PostgreSQL 기반 프로젝트 뼈대를 생성해줘. 폴더 구조는 7번 섹션을 따라줘."

---

## 2. 공통 규칙

### 2.1 Base URL
```
/api/v1
```

### 2.2 공통 응답 포맷
```json
// 성공
{
  "success": true,
  "data": { },
  "meta": { "page": 1, "size": 20, "total": 100 }
}

// 실패
{
  "success": false,
  "error": {
    "code": "NOTICE_NOT_FOUND",
    "message": "해당 공지사항을 찾을 수 없습니다."
  }
}
```

### 2.3 인증/인가
- 로그인 성공 시 Access Token(JWT, 만료 1시간) + Refresh Token(만료 14일) 발급.
- 요청 헤더: `Authorization: Bearer {accessToken}`
- 권한 레벨:
  - `USER` : 일반 회원/참가선수 (조회 중심)
  - `ADMIN` : 관리자 (등록/수정/삭제 가능) — 엑셀의 "관리자 기능 필" 항목에 대응
- 엔드포인트별 필요 권한은 각 섹션 표의 `권한` 컬럼에 명시.

### 2.4 페이지네이션 (목록 조회 공통 쿼리)
```
?page=1&size=20&sort=reg_date,desc&keyword=검색어
```

### 2.5 공통 에러 코드
| HTTP 상태 | code | 설명 |
|---|---|---|
| 400 | VALIDATION_ERROR | 요청 값 검증 실패 |
| 401 | UNAUTHORIZED | 인증 필요/토큰 만료 |
| 403 | FORBIDDEN | 권한 없음(관리자 전용 접근 등) |
| 404 | NOT_FOUND | 리소스 없음 |
| 409 | CONFLICT | 중복/충돌 (예: 이미 등록된 참가자) |
| 500 | INTERNAL_ERROR | 서버 오류 |

### 2.6 날짜/시간 포맷
- 날짜: `YYYY-MM-DD`
- 시간: `HH:mm:ss`
- 응답은 ISO 8601 (`2026-01-15T09:00:00+09:00`) 사용.

---

## 3. 데이터베이스 스키마

> 엑셀 "DB" 시트를 정규화하여 정리. `설계 버전 2026.01` 기준을 메인 스키마로 채택하고, `2025.06` 구버전(Matches/MatchResults/SetScores/MatchStats)은 참고용 레거시로 별도 표기합니다. **신규 개발은 2026.01 스키마만 사용하세요.**

### 3.1 notice (공지사항)
| 컬럼 | 타입 | 제약 | 설명 |
|---|---|---|---|
| notice_num | INT | PK, AUTO_INCREMENT | 글번호 |
| notice_title | VARCHAR(100) | NOT NULL | 글제목 |
| notice_contents | VARCHAR(500) | NOT NULL | 글내용 |
| notice_writer | VARCHAR(20) | NOT NULL | 작성자 |
| noti_reg_date | DATE | DEFAULT now() | 작성일자 |
| hit_num | INT | DEFAULT 0 | 조회수 |

### 3.2 board (게시판 기본)
| 컬럼 | 타입 | 제약 | 설명 |
|---|---|---|---|
| board_id | INT | PK, AUTO_INCREMENT | 게시번호 |
| board_title | VARCHAR(100) | NOT NULL | 게시판제목 |
| board_content | VARCHAR(500) | NOT NULL | 게시판내용 |
| board_writer | VARCHAR(20) | NOT NULL | 작성자 |
| board_reg_date | DATE | DEFAULT now() | 작성일자 |

### 3.3 comment (게시판 댓글)
| 컬럼 | 타입 | 제약 | 설명 |
|---|---|---|---|
| comment_id | INT | PK, AUTO_INCREMENT | 댓글번호 |
| board_id | INT | FK → board.board_id | 게시번호 |
| user_id | INT | FK → user.user_id | 작성 회원 |
| comment_depth | INT | DEFAULT 0 | 대댓글 깊이(0=원댓글) |
| comment_content | VARCHAR(500) | NOT NULL | 댓글내용 |
| comment_writer | VARCHAR(20) | NOT NULL | 작성자 표시명 |
| comment_reg_date | DATE | DEFAULT now() | 작성일자 |

> 원본 엑셀에는 `comment_id`가 INT/VARCHAR로 중복 기재되어 있었습니다. PK는 `comment_id INT AUTO_INCREMENT`로 통일하는 것을 권장합니다(가정).

### 3.4 user (유저: 회원/참가선수/기관)
| 컬럼 | 타입 | 제약 | 설명 |
|---|---|---|---|
| user_id | INT | PK, AUTO_INCREMENT | 회원/참가선수/기관 ID |
| user_name | VARCHAR(20) | | 닉네임/별칭 |
| real_name | VARCHAR(30) | | 실명 |
| password | VARCHAR(255) | | 비밀번호(해시 저장, bcrypt) |
| phone_number | VARCHAR(20) | | 전화번호 |
| email | VARCHAR(30) | UNIQUE | 이메일 |
| birth_date | DATE | | 생년월일 |
| gender | CHAR(1) | | 성별 (M/F) |
| open_ranking | INT | | 오픈부수 |
| region_ranking | INT | | 지역/관내부수 |
| club_name | VARCHAR(50) | | 소속 동호회/클럽/기관명 |
| user_type | VARCHAR(20) | | 구분: 동호회 / 회사 / 공공기관 (기능목록 "참가선수등록" 상세 참조, 가정 추가 컬럼) |
| profile_image | TEXT | | 프로필 사진 경로 |
| role | VARCHAR(10) | DEFAULT 'USER' | USER / ADMIN (가정 추가 컬럼, 관리자 기능 구분용) |

### 3.5 tournament (대회 리그경기)
| 컬럼 | 타입 | 제약 | 설명 |
|---|---|---|---|
| tournament_year | INT | PK(복합) | 대회 연도 |
| tournament_id | INT | PK(복합), AUTO_INCREMENT | 대회 순번 |
| tournament_name | VARCHAR(100) | NOT NULL | 대회명 |
| location | VARCHAR(100) | | 장소 |
| organizer_id | INT | FK → user.user_id | 주최 기관 |
| start_date | DATE | | 시작일 |
| end_date | DATE | | 종료일 |
| entry_fee | NUMERIC | | 참가비 |
| event_info | VARCHAR(500) | | 종목 정보 (단식/혼복 등, 예선/준결승/결승 등) |
| prize_info | VARCHAR(500) | | 시상 정보 |
| notes | VARCHAR(500) | | 기타 메모 |

### 3.6 tournament_participant (대회 참가자 — user ↔ tournament 중간 테이블)
| 컬럼 | 타입 | 제약 | 설명 |
|---|---|---|---|
| participant_id | INT | PK, AUTO_INCREMENT | 순번 |
| tournament_year | INT | FK | 대회 연도 |
| tournament_id | INT | FK | 대회 ID |
| user_id | INT | FK → user.user_id | 참가자 |
| regi_date | DATE | | 등록일 |
| final_rank | INT | | 최종순위 |
| notes | VARCHAR(500) | | 비고 |
| up_date | DATE | | 수정일 |

> 참가 신청부터 최종 순위까지 전체 과정을 추적하는 테이블입니다.

### 3.7 competition (대회 내 경기 단계: 예선/본선/결승 등)
| 컬럼 | 타입 | 제약 | 설명 |
|---|---|---|---|
| competition_id | INT | PK, AUTO_INCREMENT | 순번 |
| tournament_year | INT | FK | 대회 연도 |
| tournament_id | INT | FK | 대회 ID |
| competition_name | VARCHAR(50) | | 예선/본선/결승 토너먼트 등 |
| competition_type | VARCHAR(50) | ENUM | `ROUND_ROBIN`, `SINGLE_ELIMINATION`, `DOUBLE_ELIMINATION`, `GROUP_STAGE` |
| match_format | VARCHAR(50) | ENUM | `SINGLES`(단식), `DOUBLES`(복식), `TEAM`(단체전) |
| competition_order | INT | | 진행 순서 |
| has_groups | CHAR(1) | | 조 편성 여부 (Y/N) |
| players_per_group | INT | | 조별 인원수 |
| description | VARCHAR(200) | | 기타 |
| status | VARCHAR(20) | ENUM | `SCHEDULED`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED` |

### 3.8 group (조 편성: 복식/단체전용, has_groups='Y'일 때 사용)
| 컬럼 | 타입 | 제약 | 설명 |
|---|---|---|---|
| group_id | INT | PK, AUTO_INCREMENT | 그룹 ID |
| competition_id | INT | FK | 대상 경기 단계 |
| group_name | VARCHAR(50) | | A조/B조/C조 등 |
| creat_date | DATE | | 생성일 |
| up_date | DATE | | 수정일 |

### 3.9 group_participant (조별 참가자)
| 컬럼 | 타입 | 제약 | 설명 |
|---|---|---|---|
| group_id | INT | PK(복합), FK | 그룹 ID |
| user_id | INT | PK(복합), FK | 참가자 |
| group_rank | INT | | 조 내 최종 순위 |
| creat_date | DATE | | 생성일 |
| up_date | DATE | | 수정일 |

### 3.10 match (경기: 실제 대진/일정/스코어)
| 컬럼 | 타입 | 제약 | 설명 |
|---|---|---|---|
| match_id | INT | PK, AUTO_INCREMENT | 경기 ID |
| competition_id | INT | FK | 소속 경기 단계 |
| group_id | INT | FK, NULLABLE | 소속 조 (없으면 NULL) |
| match_round | INT | | 토너먼트 라운드 (32강=32, 16강=16, 8강=8, 4강=4, 결승=2, 개인라운드=0) |
| match_number | INT | | 같은 라운드 내 경기 번호 |
| court_number | INT | | 코트/테이블 번호 |
| status | VARCHAR(20) | ENUM | `SCHEDULED`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED` |
| winner_side | VARCHAR(10) | ENUM | `SIDE_A`, `SIDE_B`, `DRAW` |
| side_a_sets | INT | | A팀 획득 세트 수 |
| side_b_sets | INT | | B팀 획득 세트 수 |
| total_sets | INT | | 총 진행 세트 수 |
| next_match_id | INT | FK(self), NULLABLE | 토너먼트 다음 라운드 경기 |
| notes | VARCHAR(500) | | 비고 |

### 3.11 match_participant (경기 참가자)
| 컬럼 | 타입 | 제약 | 설명 |
|---|---|---|---|
| match_participant_id | INT | PK, AUTO_INCREMENT | 순번 |
| match_id | INT | FK | 경기 ID |
| user_id | INT | FK | 참가자 |
| side | VARCHAR(10) | ENUM | `SIDE_A`, `SIDE_B` |
| participant_order | INT | | 복식/단체전 내 순서 |
| creat_date | DATE | | 생성일 |
| up_date | DATE | | 수정일 |

> 단식: 양쪽 1명씩 / 복식: 양쪽 2명씩 / 단체전: 3명 이상.

### 3.12 set_score (세트별 점수) — 신규 명칭 제안, 화면예시의 "세트 점수 입력" 기능 대응
| 컬럼 | 타입 | 제약 | 설명 |
|---|---|---|---|
| set_id | INT | PK, AUTO_INCREMENT | 고유 ID |
| match_id | INT | FK → match.match_id | 경기 ID |
| set_number | INT | | 세트 번호 (1~5) |
| side_a_point | INT | | A측 점수 |
| side_b_point | INT | | B측 점수 |

> 원본 DB 시트의 `SetScores`(2025.06 레거시) 구조를 `match` 스키마(2026.01)에 맞게 재구성했습니다(가정: player1/2 → side_a/b로 치환).

### 3.13 match_stats (경기 통계 — 선택 기능)
| 컬럼 | 타입 | 제약 | 설명 |
|---|---|---|---|
| match_id | INT | FK | 경기 ID |
| user_id | INT | FK | 선수 ID |
| aces | INT | | 서브 에이스 수 |
| faults | INT | | 서브 실수 |
| rallies | INT | | 랠리 평균 횟수 |
| win_rate | FLOAT | | 승률/득점 비율 |

### 3.14 테이블 관계 요약
- `tournament` 1 — N `tournament_participant` N — 1 `user`
- `tournament` 1 — N `competition`
- `competition` 1 — N `group` (has_groups='Y'인 경우)
- `group` 1 — N `group_participant` N — 1 `user`
- `competition` 1 — N `match`
- `match` 1 — N `match_participant` N — 1 `user`
- `match` 1 — N `set_score`
- `match` 1 — N `match_stats`
- `board` 1 — N `comment`

### 3.15 ERD 생성 지시 (Codex용)
> "위 3.1~3.13 테이블 정의를 기반으로 JPA `@Entity` 클래스들을 작성하고, Flyway 마이그레이션 스크립트(`V1__init.sql`)를 생성해줘. ENUM 컬럼은 Java `enum` + `@Enumerated(EnumType.STRING)`으로 매핑해줘. 복합키(예: tournament_year+tournament_id, group_id+user_id)는 `@EmbeddedId` 또는 `@IdClass`로 처리해줘."

---

## 4. API 엔드포인트 명세

> 엑셀 "기능목록" 시트의 대분류를 기준으로 그룹화했습니다. `권한` 컬럼: `공개`=비로그인 허용, `USER`=로그인 회원, `ADMIN`=관리자 전용.

### 4.1 커뮤니티 — 공지사항

| Method | URL | 설명 | 권한 |
|---|---|---|---|
| GET | /api/v1/notices | 공지사항 목록 조회 (페이지네이션) | 공개 |
| GET | /api/v1/notices/{noticeNum} | 공지사항 상세 조회 (조회수 +1) | 공개 |
| POST | /api/v1/notices | 공지사항 등록 | ADMIN |
| PUT | /api/v1/notices/{noticeNum} | 공지사항 수정 | ADMIN |
| DELETE | /api/v1/notices/{noticeNum} | 공지사항 삭제 | ADMIN |

**등록 요청 예시**
```json
POST /api/v1/notices
{
  "noticeTitle": "2026 전국 동호인 대회 안내",
  "noticeContents": "...",
  "noticeWriter": "관리자"
}
```

### 4.2 커뮤니티 — 게시판 / 댓글

| Method | URL | 설명 | 권한 |
|---|---|---|---|
| GET | /api/v1/boards | 게시글 목록 조회 | 공개 |
| GET | /api/v1/boards/{boardId} | 게시글 상세 조회 | 공개 |
| POST | /api/v1/boards | 게시글 등록 | ADMIN |
| PUT | /api/v1/boards/{boardId} | 게시글 수정 | ADMIN |
| DELETE | /api/v1/boards/{boardId} | 게시글 삭제 | ADMIN |
| GET | /api/v1/boards/{boardId}/comments | 댓글 목록 조회 | 공개 |
| POST | /api/v1/boards/{boardId}/comments | 댓글 등록 | USER |
| PUT | /api/v1/comments/{commentId} | 댓글 수정 | USER(본인) / ADMIN |
| DELETE | /api/v1/comments/{commentId} | 댓글 삭제 | USER(본인) / ADMIN |

### 4.3 대회 경기 현황

| Method | URL | 설명 | 권한 |
|---|---|---|---|
| GET | /api/v1/tournaments | 대회(리그경기) 목록/현황 조회 | 공개 |
| GET | /api/v1/tournaments/{year}/{tournamentId} | 대회 상세 조회 (개요/내용/기간/개인전·단체전 등) | 공개 |
| POST | /api/v1/tournaments | 대회 등록 | ADMIN |
| PUT | /api/v1/tournaments/{year}/{tournamentId} | 대회 수정 | ADMIN |
| DELETE | /api/v1/tournaments/{year}/{tournamentId} | 대회 삭제 | ADMIN |

**등록 요청 예시**
```json
POST /api/v1/tournaments
{
  "tournamentYear": 2026,
  "tournamentName": "2026 상반기 동호인 리그전",
  "location": "포항체육관",
  "startDate": "2026-04-10",
  "endDate": "2026-04-12",
  "entryFee": 30000,
  "eventInfo": "단식/혼복, 예선-준결승-결승",
  "prizeInfo": "우승 상금 100만원"
}
```

### 4.4 대회 경기 참가선수

| Method | URL | 설명 | 권한 |
|---|---|---|---|
| GET | /api/v1/tournaments/{year}/{tournamentId}/participants | 참가선수 현황 조회 (선수명/단체명 검색 지원 `?keyword=`) | 공개 |
| POST | /api/v1/tournaments/{year}/{tournamentId}/participants | 참가선수 등록 (구분: 동호회/회사/공공기관) | ADMIN |
| PUT | /api/v1/tournaments/{year}/{tournamentId}/participants/{participantId} | 참가선수 정보 수정 | ADMIN |
| DELETE | /api/v1/tournaments/{year}/{tournamentId}/participants/{participantId} | 참가 취소/삭제 | ADMIN |

### 4.5 대회 경기 조편성

| Method | URL | 설명 | 권한 |
|---|---|---|---|
| GET | /api/v1/competitions/{competitionId}/groups | 조편성 조회 (리그/토너먼트 공통) | 공개 |
| POST | /api/v1/competitions/{competitionId}/groups | 조편성 등록 (대회 경기 선택 후 조 생성) | ADMIN |
| PUT | /api/v1/groups/{groupId} | 조편성 수정 | ADMIN |
| GET | /api/v1/groups/{groupId}/participants | 조별 참가선수 현황 조회 | 공개 |
| POST | /api/v1/groups/{groupId}/participants | 조별 참가선수 등록 | ADMIN |
| PUT | /api/v1/groups/{groupId}/participants/{userId} | 조별 참가선수 정보(순위 등) 수정 | ADMIN |

### 4.6 대회 경기 (Match) 및 대진표

| Method | URL | 설명 | 권한 |
|---|---|---|---|
| GET | /api/v1/competitions/{competitionId}/matches | 경기(대진) 목록 조회 | 공개 |
| GET | /api/v1/matches/{matchId} | 경기 상세 조회 (참가자/세트 스코어 포함) | 공개 |
| POST | /api/v1/matches | 경기(대진) 등록 — 선수 선택, 날짜/장소, 라운드 | ADMIN |
| PUT | /api/v1/matches/{matchId} | 경기 정보 수정 | ADMIN |
| DELETE | /api/v1/matches/{matchId} | 경기 삭제 | ADMIN |

**경기 등록 요청 예시** (화면예시 [3] 경기 등록 화면 대응)
```json
POST /api/v1/matches
{
  "competitionId": 12,
  "groupId": null,
  "matchRound": 0,
  "courtNumber": 3,
  "participants": [
    { "userId": 101, "side": "SIDE_A", "participantOrder": 1 },
    { "userId": 102, "side": "SIDE_B", "participantOrder": 1 }
  ],
  "notes": "우천으로 15분 지연"
}
```

### 4.7 대회 경기결과 (세트 점수 / 결과 등록)

| Method | URL | 설명 | 권한 |
|---|---|---|---|
| GET | /api/v1/matches/{matchId}/sets | 세트별 점수 조회 | 공개 |
| POST | /api/v1/matches/{matchId}/sets | 세트 점수 등록 (자동 승자 계산 트리거) | ADMIN |
| PUT | /api/v1/matches/{matchId}/sets/{setId} | 세트 점수 수정 | ADMIN |
| GET | /api/v1/results?type=individual\&name={name} | 개인전 경기결과 조회 (성명으로 조회) | 공개 |
| GET | /api/v1/results?type=team\&club={club} | 단체전 경기결과 조회 (단체명으로 조회) | 공개 |
| POST | /api/v1/matches/{matchId}/finalize | 경기 결과 확정 (승자/세트수/상태 COMPLETED 처리) | ADMIN |

**세트 점수 등록 요청 예시** (화면예시 [4] 세트 점수 입력 화면 대응)
```json
POST /api/v1/matches/55/sets
{
  "sets": [
    { "setNumber": 1, "sideAPoint": 11, "sideBPoint": 8 },
    { "setNumber": 2, "sideAPoint": 7,  "sideBPoint": 11 },
    { "setNumber": 3, "sideAPoint": 11, "sideBPoint": 9 }
  ]
}
```
**서버 로직 (자동 승자 계산)**
1. 세트별 점수 저장.
2. `side_a_sets`, `side_b_sets`, `total_sets` 재계산.
3. `match_format`에 따른 승리 조건 확인 (예: 단식/복식 3판 2선승 또는 5판 3선승 — 설정값으로 관리, 기본값 3선승 가정).
4. 승리 세트 수 충족 시 `winner_side` 및 `status=COMPLETED` 자동 갱신.
5. 토너먼트인 경우 `next_match_id`가 있으면 다음 라운드 경기에 승자 자동 배정 (선택 기능, 가정).

### 4.8 통계 / 랭킹 (화면예시 [6] 대응)

| Method | URL | 설명 | 권한 |
|---|---|---|---|
| GET | /api/v1/rankings?period=month\&club={club}\&gender={m/f} | 선수 랭킹표 (승률/경기수/평균득점, 기간·클럽·성별 필터) | 공개 |
| GET | /api/v1/players/{userId}/stats | 특정 선수 통계 요약 (총 경기수, 승률 등) | 공개 |
| GET | /api/v1/players/{userId}/matches | 특정 선수 최근 경기 목록 | 공개 |

### 4.9 회원/인증

> 엑셀에는 명시적 로그인 API가 없으나 `user` 테이블에 password 컬럼이 있어 인증 기능이 필요합니다(가정 추가).

| Method | URL | 설명 | 권한 |
|---|---|---|---|
| POST | /api/v1/auth/signup | 회원(참가선수) 가입 | 공개 |
| POST | /api/v1/auth/login | 로그인 (Access/Refresh 발급) | 공개 |
| POST | /api/v1/auth/refresh | 토큰 재발급 | 공개(Refresh 필요) |
| POST | /api/v1/auth/logout | 로그아웃 | USER |
| GET | /api/v1/users/{userId} | 선수/회원 프로필 조회 | 공개 |
| PUT | /api/v1/users/{userId} | 선수/회원 프로필 수정 | USER(본인)/ADMIN |
| GET | /api/v1/users?keyword=\&club=\&gender= | 선수 목록 검색 (이름/클럽/성별) — 화면예시 [2] 선수 관리 대응 | 공개 |

---

## 5. 홈 대시보드 API (화면예시 [1] 대응)

| Method | URL | 설명 |
|---|---|---|
| GET | /api/v1/dashboard/today-matches | 오늘 예정된 경기 리스트 |
| GET | /api/v1/dashboard/top-players | 상위 랭킹 선수 요약 |
| GET | /api/v1/dashboard/recent-results | 최근 경기 결과 요약 (세트별 점수, 승패) |

---

## 6. 비기능 요구사항

- **입력 검증**: 모든 POST/PUT 요청 DTO는 Jakarta Bean Validation(`@NotNull`, `@Size`, `@Pattern` 등)으로 서버단 검증하고, 검증 실패는 `GlobalExceptionHandler`에서 `VALIDATION_ERROR`(2.5)로 통일 응답.
- **동시성**: 세트 점수 등록 시 동일 경기에 대한 동시 수정 방지 (낙관적 락 또는 트랜잭션).
- **로깅**: 관리자 등록/수정/삭제 작업은 감사 로그(audit log) 기록 권장.
- **보안**: 비밀번호는 bcrypt 해시 저장, SQL Injection 방지(ORM 사용으로 기본 대응), CORS는 프론트엔드 도메인만 허용.
- **성능**: 랭킹/목록 조회는 인덱스 설계 필수 (`user_id`, `tournament_id`, `match_id`, `competition_id`).
- **테스트**: 각 도메인(공지/게시판/대회/경기/결과)별 통합 테스트 작성.

---

## 7. 폴더 구조 제안 (Codex 생성 기준)

```
backend/
├── src/main/java/com/tabletennis/app/
│   ├── config/                 # SecurityConfig, JwtConfig, SwaggerConfig, CorsConfig
│   ├── common/
│   │   ├── response/           # ApiResponse<T>, PageResponse<T> (공통 응답 래퍼)
│   │   ├── exception/          # GlobalExceptionHandler(@RestControllerAdvice), ErrorCode enum
│   │   └── util/
│   ├── domain/
│   │   ├── notice/              # NoticeController / Service / Repository / Entity / dto
│   │   ├── board/
│   │   ├── comment/
│   │   ├── user/
│   │   ├── auth/                # 로그인/회원가입/JWT 발급
│   │   ├── tournament/
│   │   ├── participant/
│   │   ├── competition/
│   │   ├── group/
│   │   ├── match/
│   │   ├── setscore/
│   │   ├── ranking/
│   │   └── dashboard/
│   │       # 각 도메인 패키지 구성: controller / service / repository / entity / dto
│   └── AppApplication.java
├── src/main/resources/
│   ├── application.yml
│   ├── application-dev.yml
│   ├── application-prod.yml
│   └── db/migration/           # Flyway: V1__init.sql, V2__...
├── src/test/java/com/tabletennis/app/
│   └── domain/...              # 도메인별 통합/단위 테스트
├── build.gradle.kts
└── settings.gradle.kts
```

**도메인 패키지 내부 표준 구조 (예: `domain/match`)**
```
match/
├── MatchController.java
├── MatchService.java
├── MatchRepository.java      # extends JpaRepository<Match, Long>
├── Match.java                 # @Entity
├── dto/
│   ├── MatchCreateRequest.java
│   ├── MatchUpdateRequest.java
│   └── MatchResponse.java
└── mapper/
    └── MatchMapper.java        # MapStruct
```

---

## 8. Docker / Docker Compose 구성

> 프론트엔드(Next.js)는 Netlify로 배포하므로 Docker 대상에서 제외합니다. **백엔드(Spring Boot) + DB(PostgreSQL)만** 컨테이너화합니다.
> 로컬 개발 환경 통일 및 향후 자체 서버(EC2 등) 배포 시 그대로 재사용 가능하도록 구성합니다.

### 8.1 Dockerfile (멀티스테이지 빌드)

```dockerfile
# backend/Dockerfile

# 1단계: 빌드
FROM gradle:8.8-jdk17 AS build
WORKDIR /app
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle ./gradle
COPY src ./src
RUN gradle bootJar --no-daemon

# 2단계: 실행 (경량 JRE 이미지)
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 8.2 docker-compose.yml (로컬 개발용)

```yaml
# backend/docker-compose.yml
version: "3.9"

services:
  postgres:
    image: postgres:15-alpine
    container_name: tt-postgres
    restart: unless-stopped
    environment:
      POSTGRES_DB: table_tennis
      POSTGRES_USER: tt_user
      POSTGRES_PASSWORD: tt_password
    ports:
      - "5432:5432"
    volumes:
      - tt_pg_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U tt_user -d table_tennis"]
      interval: 5s
      timeout: 5s
      retries: 5

  backend:
    build:
      context: .
      dockerfile: Dockerfile
    container_name: tt-backend
    restart: unless-stopped
    depends_on:
      postgres:
        condition: service_healthy
    environment:
      SPRING_PROFILES_ACTIVE: docker
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/table_tennis
      SPRING_DATASOURCE_USERNAME: tt_user
      SPRING_DATASOURCE_PASSWORD: tt_password
      JWT_SECRET: ${JWT_SECRET:-change-me-in-prod}
      # Netlify에 배포된 프론트엔드 도메인을 CORS 허용 origin으로 등록
      CORS_ALLOWED_ORIGINS: ${CORS_ALLOWED_ORIGINS:-http://localhost:3000}
    ports:
      - "8080:8080"

volumes:
  tt_pg_data:
```

### 8.3 application-docker.yml (컨테이너 환경 프로파일)

```yaml
# backend/src/main/resources/application-docker.yml
spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL}
    username: ${SPRING_DATASOURCE_USERNAME}
    password: ${SPRING_DATASOURCE_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: validate   # 스키마는 Flyway가 관리, JPA는 검증만
  flyway:
    enabled: true

jwt:
  secret: ${JWT_SECRET}

cors:
  allowed-origins: ${CORS_ALLOWED_ORIGINS}
```

### 8.4 .env.example (docker-compose와 함께 사용)

```
JWT_SECRET=local-dev-secret-please-change
CORS_ALLOWED_ORIGINS=http://localhost:3000
```

### 8.5 실행 방법

```bash
cd backend
docker compose up -d --build   # postgres + backend 동시 기동
docker compose logs -f backend # 로그 확인
docker compose down            # 종료 (볼륨 유지, 데이터 보존)
docker compose down -v         # 종료 + 볼륨 삭제 (DB 초기화)
```

### 8.6 CORS 설정 참고 (Netlify 프론트엔드 연동)

- 프론트엔드가 Netlify(예: `https://table-tennis-app.netlify.app`)에 배포되므로, 백엔드 `SecurityConfig`의 CORS 허용 origin에 **Netlify 배포 도메인**을 반드시 추가해야 합니다.
- 로컬 개발 시: `http://localhost:3000`
- 운영 배포 시: `https://<netlify-사이트명>.netlify.app` (커스텀 도메인 연결 시 해당 도메인도 추가)
- `CORS_ALLOWED_ORIGINS` 환경변수를 콤마(,)로 구분해 여러 origin을 등록할 수 있도록 `SecurityConfig`에서 파싱하는 것을 권장합니다.

### 8.7 폴더 구조에 추가되는 파일

```
backend/
├── Dockerfile
├── docker-compose.yml
├── .env.example
├── src/main/resources/
│   └── application-docker.yml
└── ...(기존 7번 섹션 구조 동일)
```

---

## 9. Codex 작업 지시 예시 (그대로 복사해서 사용 가능)

```
아래 "탁구 경기 결과 기록 관리 시스템" 백엔드 API 명세서를 기준으로
Java 17 + Spring Boot 3 + Gradle + Spring Data JPA + PostgreSQL 프로젝트를 생성해줘.

1) 3번 섹션의 테이블 정의로 JPA @Entity 클래스 작성 및 Flyway 초기 마이그레이션(V1__init.sql) 생성
2) 7번 섹션의 폴더/패키지 구조로 프로젝트 스캐폴딩 (도메인별 controller/service/repository/entity/dto)
3) 2번 섹션의 공통 응답 포맷(ApiResponse<T>)과 에러 코드(ErrorCode enum)를
   @RestControllerAdvice 기반 GlobalExceptionHandler로 구현
4) 4.9 인증 모듈 구현: Spring Security + JWT(Access/Refresh) 필터, ADMIN/USER 권한 분리
   (@PreAuthorize 또는 커스텀 어노테이션 사용)
5) 4.1(공지사항), 4.2(게시판/댓글) 모듈부터 CRUD API 구현
6) 이후 4.3~4.8 순서로 나머지 도메인 API 구현 (대회 → 참가선수 → 조편성 → 경기 → 세트점수결과 → 통계)
7) springdoc-openapi를 적용해 Swagger UI(/swagger-ui.html)로 API 문서 자동 노출
8) 8번 섹션의 Dockerfile, docker-compose.yml, application-docker.yml, .env.example을 생성하고
   `docker compose up -d --build`로 postgres+backend가 정상 기동되는지 확인
9) SecurityConfig에 CORS 설정을 추가해 Netlify에 배포될 프론트엔드 도메인을
   환경변수(CORS_ALLOWED_ORIGINS)로 허용 origin에 등록
10) 각 도메인에 대해 JUnit5 + MockMvc 통합 테스트, 필요 시 Testcontainers로 PostgreSQL 테스트 환경 구성

작업은 위 순서대로 단계별로 진행하고, 각 단계 완료 후 결과를 요약해줘.
```

---

## 10. 원본 근거 매핑 (추적용)

| 이 문서 섹션 | 엑셀 시트/근거 |
|---|---|
| 3. DB 스키마 | "DB" 시트 전체 |
| 4.1, 4.2 | "기능목록" 시트 - 커뮤니티 |
| 4.3 | "기능목록" 시트 - 대회 경기 |
| 4.4 | "기능목록" 시트 - 대회 경기 참가선수 |
| 4.5 | "기능목록" 시트 - 대회 경기 조편성 |
| 4.7 | "기능목록" 시트 - 대회 경기결과 |
| 4.6, 4.7 요청 예시 | "화면예시"/"사례" 시트 - [3][4] |
| 4.8, 5 | "화면예시" 시트 - [1][6] |
| 1. 기술 스택 | "DB" 시트 - 개발 스택 예시 |
| 8. Docker 구성 | 사용자 요청 (Netlify 프론트 배포에 맞춘 백엔드 전용 컨테이너화) |
