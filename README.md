# HOST 개발 도구

`VAN_개발자_유틸사이트_요구사항정의서_v2.md` 기준 Java Spring Boot + React 프로젝트.
사이드바(대시보드 + 6개 메뉴) 구조이며, 6개 화면 모두 프론트엔드/백엔드 연동까지 구현되어
있습니다. 다만 요구사항 정의서에서 아직 확정되지 않은 부분들은 코드에 `// 확인 필요`
주석으로 표시하고 임시 값(Mock 데이터 등)으로 채워둔 상태입니다 (화면별 상세는 하단
["화면별 구현 현황"](#화면별-구현-현황) 참고).

## 폴더 구조

```
backend/   Spring Boot (Java 17)
  src/main/java/com/example/hostdevtools/
    HostDevToolsApplication.java
    config/CorsConfig.java              CORS 허용 Origin 설정 (환경변수로 교체 가능)
    stringhex/           String ↔ Hex 변환
    bitmap/               비트맵 파서 (1차/2차 비트맵 On/Off 계산)
    cardsimulator/         카드사 시뮬레이터 (Mock 상태 관리)
    isoparsing/            ISO 전문 파싱 (고정 필드표 기반)
    hostnumbering/         호스트 채번 (운영 엑셀 업로드 + 테스트 DB 대조)
    testdatavault/         테스트 데이터함 (파일 기반 저장)
  src/main/resources/application.properties
  src/test/java/.../stringhex/StringHexServiceTest.java
  data/                  테스트 데이터함이 만드는 저장 파일(test-data-vault.json). git에는
                         커밋되지 않으며 최초 저장 시 자동 생성됩니다.

frontend/  Vite + React 18
  src/
    layout/Sidebar.jsx, Layout.jsx
    pages/  Dashboard, StringHexConvert, BitmapParser, CardSimulator,
            IsoParsing, HostNumbering, TestDataVault
    api/    화면별 axios 호출 모듈
    styles.css
```

## 사전 준비물

| 구분 | 필요한 것 | 비고 |
|---|---|---|
| 백엔드 빌드 | JDK 17+, Maven 3.6+ | 실행만 할 거면(이미 빌드된 jar) JRE 17+만 있으면 됩니다 |
| 프론트엔드 빌드 | Node.js 18+, npm | 실행만 할 거면(이미 빌드된 정적 파일) 불필요합니다 |
| (선택) 호스트 채번 테스트 DB 연동 | Oracle DB 접속 정보 | 기본은 비활성화, 아래 환경변수 참고 |

## 실행 방법

### A. 로컬 개발 (인터넷 연결 환경)

인터넷이 되는 개발 PC에서 바로 띄울 때 사용합니다. 실행할 때마다 Maven/npm이 의존성을
내려받습니다.

```bash
# 백엔드 (포트 8080)
cd backend
mvn spring-boot:run

# 프론트엔드 (포트 3000, 별도 터미널)
cd frontend
npm install
npm run dev
```

> **Windows PowerShell에서 `npm install`이 "이 시스템에서 스크립트를 실행할 수 없으므로…"
> 오류로 막힐 때**: PowerShell의 스크립트 실행 정책 때문입니다(코드 문제 아님).
> - 그때그때 우회: `npm.cmd install`처럼 `npm` 대신 `npm.cmd`를 씁니다.
> - 영구 해결: PowerShell에서 `Set-ExecutionPolicy -Scope CurrentUser -ExecutionPolicy RemoteSigned`
>   실행 (관리자 권한 불필요, 현재 계정에만 적용).

### B. 내부망(폐쇄망) 배포

내부망에는 인터넷이 없어 `mvn`/`npm`이 의존성을 받지 못하는 것을 전제로 합니다. **인터넷이
되는 환경에서 미리 빌드한 산출물만 내부망으로 옮겨서 실행**하는 방식입니다.

**1단계 — 인터넷 되는 환경(빌드 머신)에서 빌드**

```bash
# 백엔드: 의존성(Oracle JDBC, Apache POI 등)이 모두 포함된 실행 가능 jar 하나로 패키징됨
cd backend
mvn clean package -DskipTests
# → backend/target/host-dev-tools-backend-0.0.1-SNAPSHOT.jar

# 프론트엔드: 정적 파일(HTML/JS/CSS)로 빌드됨
cd ../frontend
npm install
npm run build
# → frontend/dist/ (이 폴더 전체가 결과물)
```

**2단계 — 내부망으로 옮길 파일**

- `backend/target/host-dev-tools-backend-0.0.1-SNAPSHOT.jar`
- `frontend/dist/` 폴더 전체

(백엔드 jar에는 Oracle JDBC 드라이버, Apache POI 등 모든 의존성이 이미 포함되어 있어
내부망 서버에서 별도로 다운로드할 필요가 없습니다.)

**3단계 — 내부망 서버에서 실행**

```bash
# 백엔드: JRE 17+만 있으면 실행 가능 (Maven 불필요)
java -jar host-dev-tools-backend-0.0.1-SNAPSHOT.jar
```

프론트엔드(`dist/`)는 정적 파일이라 Node 없이 아무 정적 웹서버(Nginx, IIS, Apache 등)로
서빙하면 됩니다. 내부망에 별도 웹서버가 없다면 Node가 설치된 서버 한 대에서 다음처럼 간단히
띄울 수도 있습니다.

```bash
npx serve dist -l 3000
```

**중요 — CORS 설정**: 백엔드는 기본적으로 `http://localhost:3000`에서 오는 요청만 허용합니다.
내부망에서는 프론트엔드가 다른 호스트/포트(예: `http://10.0.0.5:3000`)로 뜨므로, 백엔드
실행 시 `CORS_ALLOWED_ORIGINS` 환경변수로 실제 프론트엔드 주소를 지정해야 API 호출이
정상 동작합니다 (아래 환경변수 표 참고).

### 환경변수

민감 정보(비밀번호 등)나 배포 환경마다 달라지는 값은 코드/설정 파일에 직접 적지 않고
환경변수로 주입하도록 되어 있습니다.

| 환경변수 | 기본값 | 설명 |
|---|---|---|
| `CORS_ALLOWED_ORIGINS` | `http://localhost:3000` | 프론트엔드 접근을 허용할 Origin. 여러 개면 콤마로 구분 |
| `TEST_DB_ENABLED` | `false` | 호스트 채번 화면의 Oracle 테스트 DB 연동 여부. `false`면 Mock 데이터로 동작 |
| `TEST_DB_URL` | `jdbc:oracle:thin:@//localhost:1521/ORCLPDB1` | 테스트 DB JDBC URL |
| `TEST_DB_USERNAME` | (없음) | 테스트 DB 계정 |
| `TEST_DB_PASSWORD` | (없음) | 테스트 DB 비밀번호 |
| `TEST_DB_TABLE` | `LINE_TCP_IP_INFO` | 조회 대상 테이블명 |
| `TEST_DB_CODE_COLUMN` | `HOST_CODE` | 코드 컬럼명 (실제 컬럼명 확인 전 임시값, 아래 참고) |
| `TEST_DB_PORT_COLUMN` | `PORT_NO` | 포트 컬럼명 (실제 컬럼명 확인 전 임시값, 아래 참고) |

`TEST_DB_ENABLED=true`로 켠 뒤 `GET /api/host-numbering/test-db/columns`를 호출하면 실제
테이블의 컬럼 목록을 확인할 수 있습니다. 확인되면 `TEST_DB_CODE_COLUMN` /
`TEST_DB_PORT_COLUMN`을 실제 값으로 교체하세요.

### 데이터 저장

"테스트 데이터함" 화면은 별도 DB 없이 백엔드 실행 위치 기준 `data/test-data-vault.json`
파일에 상태(등록된 프로세스 목록 + 저장된 전문)를 그대로 읽고 씁니다. 여러 인스턴스가 동시에
같은 파일에 쓰는 상황은 고려되어 있지 않으므로, 내부망에 배포할 때는 백엔드를 단일
인스턴스로만 띄우고 `data/` 폴더에 쓰기 권한을 확인하세요. 이 파일은 git에 커밋되지 않으므로
재배포 시 필요하면 별도로 백업/이관해야 합니다.

## 화면별 구현 현황

| 화면 | API | 상태 |
|---|---|---|
| String ↔ Hex 변환 | `POST /api/string-hex/convert` | 구현 완료 |
| 비트맵 파서 | `POST /api/bitmap/parse` | 구현 완료 |
| 카드사 시뮬레이터 | `GET /api/card-simulators`, `POST /{id}/start,stop,restart` | 골격 구현 (Mock 상태 토글만, 실제 프로세스 연동 미정) |
| ISO 전문 파싱 | `POST /api/iso-parsing/parse` | 골격 구현 (일부 필드 길이 미확정) |
| 호스트 채번 | `GET /status`, `/test-db/columns`, `/production-template`, `POST /production-upload`, `/recommend` | 구현 완료 (테스트 DB 컬럼명은 임시값) |
| 테스트 데이터함 | `GET/POST /processes`, `GET/POST /`, `DELETE /{id}` | 구현 완료 (파일 기반 저장) |

### String ↔ Hex 변환

- 입력창에 `BASDKJNCVLS1213(03)(00)(F2)(3C)(24)` 처럼 일반 문자와 `(XX)` Hex 리터럴이 섞인
  문자열을 넣으면, 서버가 토큰 단위로 파싱해 `[{type, sourceStart, sourceEnd, sourceText, hex}]`
  목록과 경고 메시지를 반환합니다.
- 미리보기 줄에서 일반 문자는 기본색, `(XX)` 리터럴은 갈색으로 구분되고, 미리보기와 결과의
  같은 토큰끼리는 hover 시 함께 강조됩니다.
- "ASCII 변환" 체크박스로 일반 문자 변환 시 ASCII / EBCDIC 코드값을 선택할 수 있습니다.

### 비트맵 파서

16자리(1차) 또는 32자리(1차+2차) Hex 문자열을 받아 필드 번호 1~64(2차 존재 시 65~128)의
On/Off를 계산합니다. 1차 비트맵의 1번 비트는 관례상 2차 비트맵 존재 여부를 나타냅니다.

### 카드사 시뮬레이터

카드사별(신한/삼성/국민/현대/롯데/하나) 실행 상태를 관리합니다. 현재는 메모리 Mock 상태를
버튼으로 토글하는 골격이며, 실제 재기동 쿼리/명령 실행 위치는 요구사항 확정 후 구현 예정입니다.

### ISO 전문 파싱

실제 예시 전문을 기준으로 역산한 고정 순서/길이 필드표(HEADER/전문구분/비트맵/거래구분/
거래금액 등)를 앞에서부터 순서대로 잘라 파싱합니다. 전송일시/추적번호 등 일부 필드는 길이가
아직 정의되지 않아 자리만 잡아두었습니다.

### 호스트 채번

운영 환경 엑셀 업로드 + 테스트 DB(Oracle, 선택적 연동) 조회 결과를 모아, 양쪽 어디에도 없는
코드/포트 조합을 추천합니다. 테스트 DB 연동은 기본 비활성화이며, 켜지 않아도 Mock 데이터로
항상 동작합니다. 테이블명(`LINE_TCP_IP_INFO`)은 확정됐지만 컬럼명은 임시 가정값입니다(위
환경변수 절 참고).

### 테스트 데이터함

프로세스(예: `ongwhtion44`, `ongwhtioj13`)별로 승인거래/취소거래 등 전문을 등록·조회·삭제합니다.
DB 없이 `data/test-data-vault.json` 파일에 저장하는 단일 서버용 골격입니다.

## 확인 필요 목록 (요구사항 정의서 기준 미확정 사항)

코드에는 `// 확인 필요` 주석으로 아래 항목들을 표시해 두었습니다.

1. **String/Hex 파싱 규칙에 맞지 않는 잔여 문자 처리** — 유효하지 않은 `(XX)` 리터럴이나
   닫히지 않은 괄호는 에러로 막지 않고 `(`를 일반 문자로 처리한 뒤 계속 진행하도록
   가정했습니다 (`StringHexService.java`). 경고 메시지는 응답에 담아 화면 하단 경고 박스에
   노출합니다.
2. **"ASCII 변환" 체크박스의 정확한 의미** — 목업 이미지에는 체크박스만 있고 동작 설명이 없어,
   기본값(체크됨) = ASCII 코드값, 해제 = EBCDIC 코드값으로 가정했습니다
   (`StringHexConvert.jsx`).
3. **EBCDIC 코드표** — `StringHexService.buildEbcdicTable()`은 IBM CP037 계열을 참고한
   근사치입니다. 실제 대상 호스트가 사용하는 EBCDIC 변형과 대조 확인이 필요합니다.
4. **2바이트 이상 문자 입력** (한글 등) — 단일 바이트 범위를 벗어나면 하위 1바이트만 사용하고
   경고를 남기도록 임시 처리했습니다. 실제 정책 확인 필요.
5. **인라인 편집 하이라이트 방식** — 요구사항은 "입력창 텍스트 색으로 유형 구분"을 요구하지만,
   실시간 contentEditable 구문강조는 커서 위치 계산이 복잡해 MVP에서는 일반 `<textarea>`
   (편집용) + 별도 "미리보기" 줄(색상/hover 강조용)으로 나눠 구현했습니다.
6. **카드사 시뮬레이터의 재기동 쿼리/명령 실행 위치** — 대상 카드사 목록/초기 상태는 임시
   Mock이며, start/stop/restart 시 실제로 무엇을 호출해야 하는지는 아직 미정입니다
   (`CardSimulatorService.java`).
7. **ISO 전문 파싱의 미정 필드 길이** — 전송일시/추적번호/개시시간/개시일자/입력 유형/거래고유
   번호는 길이가 정의되지 않아 자리만 잡아두었습니다 (`IsoParsingService.java`).
8. **호스트 채번 테스트 DB 컬럼명** — 테이블명(`LINE_TCP_IP_INFO`)은 확정됐지만 컬럼명은
   몰라서 `HOST_CODE`/`PORT_NO`로 임시 가정해뒀습니다. `TEST_DB_ENABLED=true`로 켠 뒤
   `GET /api/host-numbering/test-db/columns`로 확인 후 환경변수로 교체해야 합니다.
9. **테스트 데이터함의 동시 쓰기 처리** — 여러 백엔드 인스턴스가 동시에 같은
   `data/test-data-vault.json` 파일에 쓰는 상황은 고려하지 않았습니다. 단일 서버 운영을
   전제로 합니다.
10. **대시보드 카드의 상태 요약** — 각 화면의 실제 운영 상태를 요약해 보여주는 로직은 아직
    반영되지 않았습니다.
