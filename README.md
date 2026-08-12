# VAN 개발자 유틸 사이트

`VAN_개발자_유틸사이트_요구사항정의서_v2.md` 기준 Java Spring Boot + React 프로젝트 뼈대.
사이드바(대시보드 + 6개 메뉴) 라우팅 구조를 잡고, 오늘은 **`07_String_Hex변환`** 화면(B안:
인라인 하이라이트 스타일)만 실제로 구현했습니다. 나머지 5개 화면(카드사 시뮬레이터 / ISO 전문
파싱 / 비트맵 파서 / 호스트 채번 / 테스트 데이터함)은 라우팅만 연결된 placeholder 상태입니다.

## 폴더 구조

```
backend/   Spring Boot (Java 17)
  src/main/java/com/example/vantools/
    VanToolsApplication.java
    config/CorsConfig.java
    stringhex/           String → Hex 변환 기능
      StringHexController.java
      StringHexService.java   (파싱/EBCDIC 변환 로직)
      StringHexToken.java
      StringHexConvertRequest.java / StringHexConvertResponse.java
  src/test/java/.../stringhex/StringHexServiceTest.java

frontend/  Vite + React 18
  src/
    layout/Sidebar.jsx, Layout.jsx
    pages/Dashboard.jsx, Placeholder.jsx, StringHexConvert.jsx
    api/stringHexApi.js
    styles.css
```

## 실행 방법

### 백엔드 (포트 8080)
```bash
cd backend
mvn spring-boot:run
```
※ 이 환경에는 Java/Maven이 설치되어 있지 않아 직접 컴파일 검증은 못 했습니다. 로컬에 JDK 17+ /
Maven 설치 후 실행해 주세요.

### 프론트엔드 (포트 3000)
```bash
cd frontend
npm install
npm run dev
```

## 07_String_Hex변환 화면 구현 내용

- 입력창에 `BASDKJNCVLS1213(03)(00)(F2)(3C)(24)` 처럼 일반 문자와 `(XX)` Hex 리터럴이 섞인
  문자열을 넣으면, 서버(`POST /api/string-hex/convert`)가 토큰 단위로 파싱해
  `[{type, sourceStart, sourceEnd, sourceText, hex}]` 목록과 경고 메시지를 반환합니다.
- "미리보기" 줄에서 일반 문자는 기본색, `(XX)` 리터럴은 갈색으로 구분해서 보여줍니다.
- 미리보기의 글자와 "결과"의 Hex 바이트는 같은 토큰 인덱스를 공유하므로, 어느 쪽에 마우스를
  올려도 대응하는 반대쪽이 함께 파란색으로 강조됩니다.
- "ASCII 변환" 체크박스로 일반 문자 변환 시 ASCII / EBCDIC 코드값을 선택할 수 있습니다.

## 확인 필요 목록 (요구사항 정의서 기준 미확정 사항)

코드에는 `// 확인 필요` 주석으로 아래 항목들을 표시해 두었습니다.

1. **파싱 규칙에 맞지 않는 잔여 문자 처리** — 요구사항 정의서 7번 항목의 미확정 사항. 유효하지
   않은 `(XX)` 리터럴이나 닫히지 않은 괄호는 에러로 막지 않고 `(`를 일반 문자로 처리한 뒤 계속
   진행하도록 가정했습니다 (`StringHexService.java`). 경고 메시지는 응답에 담아 화면 하단
   경고 박스에 노출합니다.
2. **"ASCII 변환" 체크박스의 정확한 의미** — 목업 이미지에는 체크박스만 있고 동작 설명이 없어,
   기본값(체크됨) = ASCII 코드값, 해제 = EBCDIC 코드값으로 가정했습니다
   (`StringHexConvert.jsx`).
3. **EBCDIC 코드표** — `StringHexService.buildEbcdicTable()`은 IBM CP037 계열을 참고한
   근사치입니다. 실제 대상 호스트가 사용하는 EBCDIC 변형과 대조 확인이 필요합니다. 특히
   대괄호/역슬래시/중괄호 등 특수문자는 코드페이지별로 값이 다를 수 있습니다.
4. **2바이트 이상 문자 입력** (한글 등) — 단일 바이트 범위를 벗어나면 하위 1바이트만 사용하고
   경고를 남기도록 임시 처리했습니다. 실제 정책 확인 필요.
5. **인라인 편집 하이라이트 방식** — 요구사항은 "입력창 텍스트 색으로 유형 구분"을 요구하지만,
   실시간 contentEditable 구문강조는 커서 위치 계산이 복잡해 MVP에서는 일반 `<textarea>`
   (편집용) + 별도 "미리보기" 줄(색상/hover 강조용)으로 나눠 구현했습니다. 목업처럼 입력창
   자체에 색이 바로 보이길 원하면 추가 작업이 필요합니다.
6. **다른 화면들의 실제 구현** — 대시보드 카드의 상태 요약, 카드사 시뮬레이터의 재기동 쿼리/명령
   실행 위치, 호스트 채번의 조회 쿼리, ISO 전문 파싱의 미정 필드 등은 요구사항 정의서에 이미
   "확인 필요"로 표시된 항목이며 이번 작업 범위에는 포함하지 않았습니다.
