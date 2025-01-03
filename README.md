# Sportify 
![image (12)](https://github.com/user-attachments/assets/424940e9-016c-4a11-a840-d2d3f88f6fdd)

## 팀 소개
| 이름 | 역할 | 담당 업무 |
|--------|--------|------------------------------------------------------------------|
| 조연우 | 팀장 | 유저 인증/인가 API, Kafka를 활용한 실시간 경기 알림 기능 구현 |
| 김학진 | 부팀장 | 예약 API, 쿠폰 API, Redis를 활용한 동시성 구축 |
| 박정완 | 팀원 | 카카오페이 결제 시스템 구축, 팀 및 팀멤버 API |
| 정현우 | 팀원 | 매치 조회, 점수 부여, 레디스를 활용한 캐싱 |
| 조중휘 | 팀원 | 구장 API, 구장 영업 시간 API, 매치 조회(구장 별, 날짜별), 팀 게시판, 팀 채팅 |
## 시스템 구성도
![sportify 인프라 아키텍처 drawio](https://github.com/user-attachments/assets/9e3fc3a6-37ec-4eb7-babc-e5e4241be467)

## 와이어프레임
<img width="1040" alt="스크린샷 2025-01-03 오후 7 28 48" src="https://github.com/user-attachments/assets/6eb6d748-4934-4334-9056-bc294cff5f92" />
<img width="1037" alt="스크린샷 2025-01-03 오후 7 29 08" src="https://github.com/user-attachments/assets/469c396b-161f-4f2c-a936-1ca6fa1230b6" />

## ERD
![image (15)](https://github.com/user-attachments/assets/27135c06-09c5-449d-be97-92ab4b1cb747)

## 개발환경

### 사용 언어
![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring](https://img.shields.io/badge/spring-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-black?style=for-the-badge&logo=JSON%20web%20tokens)
<img src="https://img.shields.io/badge/JPA-6DB33F?style=for-the-badge&logo=java&logoColor=white">
<img src="https://img.shields.io/badge/junit5-25A162?style=for-the-badge&logo=junit5&logoColor=white">
### 운영 환경
![Ubuntu](https://img.shields.io/badge/Ubuntu-E95420?style=for-the-badge&logo=ubuntu&logoColor=white)
![AWS](https://img.shields.io/badge/AWS-%23FF9900.svg?style=for-the-badge&logo=amazon-aws&logoColor=white)
<img src="https://img.shields.io/badge/EC2-FF9900?style=for-the-badge&logo=amazonec2&logoColor=white">
### 개발환경 & 데이터베이스
![IntelliJ IDEA](https://img.shields.io/badge/IntelliJIDEA-000000.svg?style=for-the-badge&logo=intellij-idea&logoColor=white)
<img src="https://img.shields.io/badge/RDS-527FFF?style=for-the-badge&logo=amazonrds&logoColor=white">
![Apache Kafka](https://img.shields.io/badge/Apache%20Kafka-000?style=for-the-badge&logo=apachekafka)
![MySQL](https://img.shields.io/badge/mysql-4479A1.svg?style=for-the-badge&logo=mysql&logoColor=white)
![Redis](https://img.shields.io/badge/redis-%23DD0031.svg?style=for-the-badge&logo=redis&logoColor=white)
### 외부 API

<img src="https://img.shields.io/badge/Kakao-FFCD00?style=for-the-badge&logo=kakaotalk&logoColor=black"><img src="https://img.shields.io/badge/Naver-03C75A?style=for-the-badge&logo=Naver&logoColor=white">
<img src="https://img.shields.io/badge/Google-4285F4?style=for-the-badge&logo=Google&logoColor=white">
<img src="https://img.shields.io/badge/github-181717?style=for-the-badge&logo=github&logoColor=white">

### 협업 툴
![Jira](https://img.shields.io/badge/jira-%230A0FFF.svg?style=for-the-badge&logo=jira&logoColor=white)
![Notion](https://img.shields.io/badge/Notion-%23000000.svg?style=for-the-badge&logo=notion&logoColor=white)
## API 명세서

<table border="1">
  <thead>
    <tr>
      <th>기능</th>
      <th>HTTP Method</th>
      <th>URL</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>유저 회원가입</td>
      <td>POST</td>
      <td>/api/users/signup</td>
    </tr>
    <tr>
      <td>유저 로그인</td>
      <td>POST</td>
      <td>/api/users/login</td>
    </tr>
    <tr>
      <td>유저 OAuth 로그인</td>
      <td>POST</td>
      <td>/api/users/{OAuth}/login</td>
    </tr>
    <tr>
      <td>유저 정보 단건 조회</td>
      <td>GET</td>
      <td>/api/users/{userId}</td>
    </tr>
    <tr>
      <td>자신의 정보 조회</td>
      <td>GET</td>
      <td>/api/users/profile</td>
    </tr>
    <tr>
      <td>유저 삭제</td>
      <td>DELETE</td>
      <td>/api/users/{userId}</td>
    </tr>
    <tr>
      <td>유저 정보 수정</td>
      <td>PATCH</td>
      <td>/api/users/profile</td>
    </tr>
    <tr>
      <td>유저의 팀 조회</td>
      <td>GET</td>
      <td>/api/users/teams</td>
    </tr>
    <tr>
      <td>캐시 충전</td>
      <td>POST</td>
      <td>/api/users/{userId}/cash/charge</td>
    </tr>
    <tr>
      <td>캐시 내역 조회</td>
      <td>GET</td>
      <td>/api/users/{userId}/cash/transaction</td>
    </tr>
    <tr>
      <td>팀 생성</td>
      <td>POST</td>
      <td>/api/teams/register</td>
    </tr>
    <tr>
      <td>팀 전체 조회</td>
      <td>GET</td>
      <td>/api/teams/</td>
    </tr>
    <tr>
      <td>팀 단건 조회</td>
      <td>GET</td>
      <td>/api/teams/{teamId}</td>
    </tr>
    <tr>
      <td>팀 수정</td>
      <td>PATCH</td>
      <td>/api/teams/{teamId}</td>
    </tr>
    <tr>
      <td>팀 삭제</td>
      <td>DELETE</td>
      <td>/api/teams/{teamId}</td>
    </tr>
    <tr>
      <td>팀원 목록 조회</td>
      <td>GET</td>
      <td>/api/teams/{teamId}/members</td>
    </tr>
    <tr>
      <td>팀 관리-권한 부여</td>
      <td>PATCH</td>
      <td>/api/teams/grant/{userId}</td>
    </tr>
    <tr>
      <td>팀 신청</td>
      <td>POST</td>
      <td>/api/teams/grant/{teamId}</td>
    </tr>
    <tr>
      <td>팀 관리-신청 승인/거부</td>
      <td>POST</td>
      <td>/api/teams/approve/{teamId}</td>
    </tr>
    <tr>
      <td>팀 관리-퇴출</td>
      <td>DELETE</td>
      <td>/api/teams/{teamId}/reject/{userId}</td>
    </tr>
    <tr>
      <td>경기 예약(팀/개인)</td>
      <td>POST</td>
      <td>/api/reservations</td>
    </tr>
    <tr>
      <td>예약 취소</td>
      <td>DELETE</td>
      <td>/api/reservations/{reservationId}</td>
    </tr>
    <tr>
      <td>예약 내역 전체조회</td>
      <td>GET</td>
      <td>/api/reservations</td>
    </tr>
    <tr>
      <td>예약 내역 단건조회</td>
      <td>GET</td>
      <td>/api/reservations/{reservationId}</td>
    </tr>
    <tr>
      <td>날짜 별 경기 매치 조회(리스트)</td>
      <td>GET</td>
      <td>/api/matches</td>
    </tr>
    <tr>
      <td>매치 단건 조회</td>
      <td>GET</td>
      <td>/api/matches/{날짜}/{시간}</td>
    </tr>
    <tr>
      <td>경기 결과 기록</td>
      <td>POST</td>
      <td>/api/matches/result/{matchId}</td>
    </tr>
    <tr>
      <td>경기 결과 조회</td>
      <td>GET</td>
      <td>/api/matches/result/{matchResultId} or {matchId}/</td>
    </tr>
    <tr>
      <td>구장 생성</td>
      <td>POST</td>
      <td>/api/stadiumTime/{stadiumId}</td>
    </tr>
    <tr>
      <td>구장 목록 조회 (자신이 생성한)</td>
      <td>GET</td>
      <td>/api/stadium</td>
    </tr>
    <tr>
      <td>구장 수정</td>
      <td>PATCH</td>
      <td>/api/stadium/{stadiumId}</td>
    </tr>
    <tr>
      <td>구장 삭제</td>
      <td>DELETE</td>
      <td>/api/stadium/{stadiumId}</td>
    </tr>
    <tr>
      <td>구장 타임 생성</td>
      <td>POST</td>
      <td>/api/stadium</td>
    </tr>
    <tr>
      <td>구장 타임 수정</td>
      <td>PATCH</td>
      <td>/api/stadium/{stadiumTimeId}</td>
    </tr>
    <tr>
      <td>구장에 예약된 매치 조회</td>
      <td>GET</td>
      <td>/api/stadium/{stadiumId}/reservation</td>
    </tr>
    <tr>
      <td>쿠폰 생성 (관리자)</td>
      <td>POST</td>
      <td>/api/admin/coupon</td>
    </tr>
    <tr>
      <td>쿠폰 목록 (관리자)</td>
      <td>GET</td>
      <td>/api/admin/coupon</td>
    </tr>
    <tr>
      <td>쿠폰 사용 내역</td>
      <td>GET</td>
      <td>/api/coupon</td>
    </tr>
    <tr>
      <td>쿠폰 사용</td>
      <td>POST</td>
      <td>/api/coupon</td>
    </tr>
    <tr>
      <td>채팅방 입장</td>
      <td>POST</td>
      <td>/api/team/chat/join/{teamId}</td>
    </tr>
    <tr>
      <td>채팅 내역 조회</td>
      <td>GET</td>
      <td>/api/team/chat/{teamId}</td>
    </tr>
    <tr>
      <td>팀 게시판 - 게시글 생성</td>
      <td>POST</td>
      <td>/api/team/article/{teamId}</td>
    </tr>
    <tr>
      <td>팀 게시판 - 게시글 조회</td>
      <td>GET</td>
      <td>/api/team/article/{teamId}</td>
    </tr>
    <tr>
      <td>팀 게시판 - 게시글 단건 조회</td>
      <td>GET</td>
      <td>/api/team/article/{teamId}/{articleId}</td>
    </tr>
    <tr>
      <td>팀 게시판 - 게시글 수정</td>
      <td>PATCH</td>
      <td>/api/team/article/{articleId}</td>
    </tr>
    <tr>
      <td>팀 게시판 - 게시글 삭제</td>
      <td>DELETE</td>
      <td>/api/team/article/{articleId}</td>
    </tr>
    <tr>
      <td>카카오페이 결제 요청</td>
      <td>POST</td>
      <td>/api/cash/charge</td>
    </tr>
    <tr>
      <td>카카오페이 결제 승인</td>
      <td>GET</td>
      <td>/api/cash/success</td>
    </tr>
    <tr>
      <td>카카오페이 환불</td>
      <td>POST</td>
      <td>/api/cash/cancel</td>
    </tr>
    <tr>
      <td>SSE 연결 요청</td>
      <td>GET</td>
      <td>/v1/sse/subscribe</td>
    </tr>
    <tr>
      <td>특정 사용자에 대한 SSE 연결 요청</td>
      <td>GET</td>
      <td>/sse/{userId}</td>
    </tr>
    <tr>
      <td>SSE로 연결된 모든 클라이언트에게 broadcasting</td>
      <td>POST</td>
      <td>/v1/sse/broadcast</td>
    </tr>
    <tr>
      <td>특정 사용자에게 알림 메시지 전송</td>
      <td>GET</td>
      <td>/send/{userId}</td>
    </tr>
  </tbody>
</table>









## 프로젝트 구조
```
'Sportify'
    ├─sparta
    │   └─sportify
    │       ├─annotation       # 커스텀 어노테이션을 정의하는 패키지
    │       ├─aspect           # AOP 관련 기능을 구현하는 패키지
    │       ├─config           # 프로젝트 전반의 설정 파일들을 포함하는 패키지
    │       │  └─websocket     # WebSocket 통신과 관련된 설정 및 구성 파일
    │       ├─controller       # 클라이언트 요청을 처리하고 응답을 반환하는 컨트롤러 클래스들
    │       ├─dto              # 요청과 응답에 사용되는 데이터 전송 객체 (DTO)를 정의하는 패키지
    │       ├─entity           # JPA 엔티티 클래스들을 정의하는 패키지
    │       ├─exception        # 커스텀 예외와 예외 처리를 위한 클래스들
    │       ├─jwt              # JWT 토큰 생성, 검증, 관리와 관련된 클래스들
    │       ├─repository       # 데이터베이스와의 상호작용을 처리하는 JPA 레포지토리 클래스들
    │       ├─security         # Spring Security와 관련된 설정 및 보안 기능 구현 클래스들
    │       ├─service          # 비즈니스 로직을 처리하는 서비스 클래스들
    │       └─util             # 다양한 공통 유틸리티 클래스들을 포함하는 패키지
    │           ├─api          # API 결과값 처리를 위한 유틸리티 클래스들
    │           ├─cron         # 크론식 관련 계산 기능을 구현하는 클래스들
    │           └─payment      # 결제 기능과 관련된 유틸리티 클래스들
    └─test
        ├─controller           # 컨트롤러 클래스들의 단위 및 통합 테스트 클래스들
        ├─service              # 서비스 클래스들의 단위 테스트 클래스들
        └─util                 # 유틸리티 클래스들의 단위 테스트 클래스들
```
