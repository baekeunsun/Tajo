# Tajo
산업시스템공학부 2021년 종합설계 4조 프로젝트 착용감지 · 도난방지 헬멧 연동 어플


## 프로젝트 소개
킥보드 이용자 수가 늘어남에 따라 관련 교통사고 증가
이용자는 휴대 불편, 귀찮음 등을 이유로 기업이 헬멧을 제공해야한다는 입장
그러나, 킥보드의 헬멧 분실률은 70% 이상이다.

이를 해결하고자 킥보드의 헬멧을 어플과 연동하여 착용감지 · 도난방지를 알 수 있게 도와줌


## 전체 구조
![image](https://user-images.githubusercontent.com/72916415/202369809-9f50ad59-7563-47e4-99ac-166a2eeb7ce7.png)

## 어플
### 어플 개발 도구
Android studio \
Firebase


### 주요 기능
- 어플 로그인 시 자동으로 Bluetooth ON
- 연결하기
  - Connect 버튼 클릭 시 기존 페어링 목록에서 연결 가능
- 도난 시 firebase에 사용자 정보 전송

## 아두이노
### 주요 기능 
- 킥보드, 헬멧 Bluetooth 연결
- 도난 인지
  - 킥보드와 헬멧의 거리가 멀어지면 Bluetooth 연결 해제
  - 경보음 발생
  - 킥보드와 Bluetooth 재연결될 때까지 반복


## 결과물
![image](https://user-images.githubusercontent.com/72916415/202370393-8a9aaab9-b2bd-44e3-ad3a-edc0211449c5.png)

### 케이스
![image](https://user-images.githubusercontent.com/72916415/202370023-655a3e4a-ca4f-4086-b279-30d93c2b9f25.png)

### 어플
![image](https://user-images.githubusercontent.com/72916415/202370321-19478d44-597f-4f40-a1c5-eacb05bdba48.png)
