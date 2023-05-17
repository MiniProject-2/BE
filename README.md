# Need More Task 

<br/>

## STAKS
<img src="https://img.shields.io/badge/Spring-6DB33F?style=for-the-badge&logo=spring&logoColor=white"/> <img src="https://img.shields.io/badge/Spring_Boot-F2F4F9?style=for-the-badge&logo=spring-boot"/> <img src="https://img.shields.io/badge/MySQL-005C84?style=for-the-badge&logo=mysql&logoColor=white"/> <img src="https://img.shields.io/badge/Junit5-25A162?style=for-the-badge&logo=junit5&logoColor=white"/> <img src="https://img.shields.io/badge/Docker-2CA5E0?style=for-the-badge&logo=docker&logoColor=white"/> <img src="https://img.shields.io/badge/Jenkins-D24939?style=for-the-badge&logo=Jenkins&logoColor=white"/> <img src="https://img.shields.io/badge/Amazon_AWS-FF9900?style=for-the-badge&logo=amazonaws&logoColor=white"/> <img src="https://img.shields.io/badge/gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white"/> <img src="https://img.shields.io/badge/IntelliJ_IDEA-000000.svg?style=for-the-badge&logo=intellij-idea&logoColor=white"/> <img src="https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=JSON%20web%20tokens&logoColor=white"/>

<br/>

## ERD
![ERD](https://github.com/MiniProject-2/need-more-task-be/assets/107831692/a90e5c90-c97d-4c32-b45c-3ae619416869)

<br/>

## 아키텍처
![architecture](https://github.com/MiniProject-2/need-more-task-be/assets/107831692/177b05bb-ee47-4bfc-b153-bbd2e79358d9)

<br/>

## 핵심 기능
- 일정 관리(등록, 수정, 삭제)
    - 일정 시작 날짜와 끝나는 날짜를 등록
    - 일정에 관련된 사람도 등록(Asignee)
        - 다른 사용자를 이름으로 검색
- 일정 상세 관리
    - 진행 여부에 따라 Todo, In Progress, Done
    - 중요도에 따라 Low, Medium, High, Urgent
- 전체 일정 월별 조회(Calendar), 전체 일정 주간 조회(Overview Period), 전체 일정 일별 조회(Overview Daily), 나와 관련된 일정 조회(Kanban)
- 관리자 권한
    - 권한(Role) 변경 및 모든 사용자의 일정을 수정, 삭제 가능
    - 모든 사용자의 정보 조회 가능(비밀번호 제외)
- 일정 통계 조회(DashBoad)
    - 최근 일주일동안 생성된 일정의 진행여부 통계(Progress)
    - 최근 2주일동안 진행 완료된 일정의 통계(Performance)
    - 최근 일주일 일정 조회(Latest Task)   

<br/>

## 사용 기술
| 사용 기술 | 기술 설명 |
| --- | --- |
| JPA | 자바에서 객체와 관계형 데이터베이스를 매핑하기 위해 사용 |
| Spring Security | 인증, 인가를 관리하기 위해 사용 |
| Docker | 애플리케이션을 컨테이너화하여 같은 환경에서 배포 및 실행하기 위해 사용 |
| Jenkins | CI/CD |
| JWT | JWT를 쉽게 만들고 검증하기 위해 사용 |
| JUnit | 단위 테스트를 위해 사용(Mockito와 함께) |
| JaCoCo | 테스트 커버리지를 측정하기 위해 사용 |
| Lombok | 반복적인 코드 작성(getter, constructor등)을 줄이기 위해 사용 |
| MySQL | 가장 익숙한 RDBMS로, 데이터 저장 및 검색을 위해 사용 |
| S3 | 프로필 이미지를 업로드하기 위해 사용 |

<br/>

## 트러블 슈팅
- Timestamp
    - 매번 createdAt, updatedAt 멤버 변수와 메소드를 만들어줘야 하는 문제
    - 메인 메소드 클래스에 `@EnableJpaAuditing` 을 붙이고 Timestamped라는 추상화 클래스를 만들어서 createdAt, updatedAt이 필요한 엔티티가 상속받아서 사용
- ZonedDateTime
    - LocalDateTime을 쓰면 서버 시간에 맞춰지는 문제 → 서버 타임존을 변경해야 함
    - ZonedDateTime을 쓰고 ZoneId를 전역 변수로 설정해서 타임존을 고정함
- Logout 처리
    - access token의 만료기간을 로그아웃한 시점보다 1초 전으로 변경하여 만료시킴
- Pageable 쓸 때 join fetch 문제
    - Pageable을 쓸 때 join fetch를 하게 되면 countQuery를 별도로 적어줘야 하는 문제
    - countQuery를 사용할 때 조건(where)을 따로 적어주지 않아도 value의 조건을 가져옴
- 일정 작성 시 시작 날짜가 끝나는 날짜보다 뒤일 경우
    - request DTO에서 boolean으로 @AssertTrue로 검증

<br/>

## Member
| 포지션 | 이름 | 담당 | GitHub |
| --- | --- | --- | --- |
| `BE` `팀장` | 박주영 | - CI / CD<br/>- 로그인 / 로그아웃 - 유저 조회 / 검색<br/>- 개인정보 수정<br/>- 일정 CRUD<br/>- 프로필 업로드(S3) | https://github.com/ju-ei8ht |
| `BE` | 이윤형 | - 회원 권한 수정<br/>- 대시보드 통계<br/>- 칸반보드 READ<br/>- 달력 페이지 | https://github.com/Lee-yh2 |
| `BE` | 김형준 | - 회원가입 | https://github.com/jerok-kim |
| `BE` | 예병창 | - 비밀번호 확인<br/>- 이메일 중복 | https://github.com/KORYEcan |
