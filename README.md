MistServer AddStream API
========================
### 실행환경
- 전자정부프레임워크 3.10.0
- maven-repository 3.10.0
- jdk 1.8
### MistServer API 호출 흐름
1. Challenge 요청 → challenge 값 추출
   1. 요청
    - {"authorize": {"username": "<사용자명>","password": "<비밀번호>"}}
    2) 응답
    - {"authorize": {"status": "CHALL","challenge": "<challenge 값>"}}
3. MD5 해시 계산 → 인증 정보 생성
  2-1. MD5 해싱
    - String hashedPassword = DigestUtils.md5Hex(DigestUtils.md5Hex(<비밀번호>) + <challenge 값>);
  2-2. 요청
    - {"authorize": {"username": "mist","password": "<해시된 비밀번호>"}}
4. 스트림 등록 요청 → 스트림 이름과 소스 경로 전달
  3-1. 요청
     - {"addstream": {"<스트림명>": {"name": "<스트림명>","source": "<파일경로>"}}}
