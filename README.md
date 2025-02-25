MistServer AddStream API
-------------------------
# 실행환경
- 전자정부프레임워크 3.10.0
- maven-repository 3.10.0
- jdk 1.8
# API 요청 순서
#인증 요청
-X POST http://192.168.50.20:4242/api -H "Content-Type: application/json" -d "{\"command\":{\"authorize\":{\"username\":\"mist\",\"password\":\"mist\"}}}"
1단계 응답
{"authorize":{"challenge":"ddeb19c529a2310b89b41bb2275444f7","status":"CHALL"}}

2단계: 해시된 비밀번호 생성
echo -n "$(echo -n 'mist' | md5sum | awk '{print $1}')ddeb19c529a2310b89b41bb2275444f7" | md5sum | awk '{print $1}'
2단계 응답
5e6a94f845ebdd90e2e5ebdd2fd99873

3단계: 인증 완료
curl -X POST http://192.168.50.20:4242/api -H "Content-Type: application/json" -d "{\"command\":{\"authorize\":{\"username\":\"mist\",\"password\":\"5e6a94f845ebdd90e2e5ebdd2fd99873\"}}}"

4단계: 스트림 등록
curl -X POST http://192.168.50.20:4242/api -H "Content-Type: application/json" -d "{\"command\":{\"addstream\":{\"name\":\"example_stream\",\"source\":\"local:///app/media/153976-817104245 (3).mp4\"}}}"
