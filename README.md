MistServer AddStream API
-------------------------
# 실행환경
- 전자정부프레임워크 3.10.0
- maven-repository 3.10.0
- jdk 1.8
# MistServer API 호출 흐름
1. Challenge 요청 → challenge 값 추출
- {
  "authorize": {
    "username": "mist",
    "password": ""
  }
}
2. MD5 해시 계산 → 인증 정보 생성

3. 스트림 등록 요청 → 스트림 이름과 소스 경로 전달.
