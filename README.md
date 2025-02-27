MistServer AddStream API
========================
### 실행환경
- 전자정부프레임워크 3.10.0
- maven-repository 3.10.0
- jdk 1.8
### MistServer API 호출 흐름
1. Challenge 요청 → challenge 값 추출
    - 요청
        - {"authorize": {"username": "<사용자명>","password": "<비밀번호>"}}
    - 응답
        - {"authorize": {"status": "CHALL","challenge": "<challenge 값>"}}
2. MD5 해시 계산 → 인증 정보 생성
    - MD5 해싱
        - String hashedPassword = DigestUtils.md5Hex(DigestUtils.md5Hex(<비밀번호>) + <challenge 값>);
    - 요청
        - {"authorize": {"username": "mist","password": "<해시된 비밀번호>"}}
3. 스트림 등록 요청 → 스트림 이름과 소스 경로 전달
    - 요청
        - {"addstream": {"<스트림명>": {"name": "<스트림명>","source": "<파일경로>"}}}

|특징|MP4|MPEG-TS|
| :-:  | :-: | :-: |
|목|저장 및 재생용(온디맨드 콘텐츠에 적합)|실시간 스트리밍에 최적화|
|구조|‘moov’(메타데이터) + ‘mdat’(미디어 데이터)|패킷 기반 데이터 구조|
|스트림 동기화|타임스탬프 불일치 가능성 있음|패킷 단위로 동기화 정보 제공|
|MistServer 호환성|특정 설정(타임스탬프, 키프레임 등)이 맞지 않으면 오류 발생 가능|실시간 스트리밍 환경에서 안정적으로 작동|
|재생 시작 속도|‘faststart’ 옵션 필요|즉각적인 재생 가능|
|재생|재생바 조작 시 리로드|재생바 조작 시 정상 수행|
