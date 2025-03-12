# 프로젝트 개요

이 프로젝트는 SftpConnection과 AutoStreamTs 클래스를 포함하여 SFTP 서버와 MistServer를 활용한 스트림 관리 및 파일 업로드를 자동화합니다.

- SftpConnection: SFTP 서버와의 연결을 관리하고 파일을 업로드하는 유틸리티 클래스입니다.
- AutoStreamTs: 지정된 디렉토리를 모니터링하여 새로운 MP4 파일을 감지하고, 이를 TS 형식으로 변환한 후 SFTP 서버에 업로드하고 MistServer에 스트림을 추가합니다.

## 주요 기능

##### SftpConnection 클래스

- SFTP 연결: SFTP 서버에 연결하여 파일 전송 작업을 수행합니다.
- 파일 업로드: 로컬 파일을 원격 서버로 업로드합니다.
- 자원 관리: AutoCloseable을 구현하여 try-with-resources 구문으로 안전하게 연결을 닫습니다.

##### AutoStreamTs 클래스

- 디렉토리 모니터링: 지정된 디렉토리를 실시간으로 감시하여 새로운 MP4 파일 추가를 감지합니다.
- MP4 → TS 변환: FFmpeg를 사용하여 MP4 파일을 TS 형식으로 변환합니다.
- 파일 업로드: 변환된 TS 파일을 SFTP 서버로 업로드합니다.
- MistServer 스트림 추가: MistServer API를 통해 업로드된 TS 파일을 스트림으로 등록합니다.

## 실행 방법

프로그램을 실행하면 지정된 디렉토리를 실시간으로 모니터링하며, MP4 파일이 추가될 때 자동으로 처리됩니다.

## 주의 사항

##### FFmpeg 설치

- FFmpeg가 시스템에 설치되어 있어야 합니다.

##### MistServer 설정

- MistServer API가 활성화되어 있어야 하며, 인증 정보를 정확히 입력해야 합니다.

| 특징              | MP4                                                             | MPEG-TS                                  |
| ----------------- | --------------------------------------------------------------- | ---------------------------------------- |
| 컨테이너 목적     | 저장 및 재생용 (온디맨드 콘텐츠에 적합)                         | 실시간 스트리밍에 최적화                 |
| 구조              | `moov`(메타데이터) + `mdat`(미디어 데이터)                      | 패킷 기반 데이터 구조                    |
| 스트림 동기화     | 타임스탬프 불일치 가능성 있음                                   | 패킷 단위로 동기화 정보 제공             |
| MistServer 호환성 | 특정 설정(타임스탬프, 키프레임 등)이 맞지 않으면 오류 발생 가능 | 실시간 스트리밍 환경에서 안정적으로 작동 |
| 재생 시작 속도    | `faststart` 옵션 필요                                           | 즉각적인 재생 가능                       |
| 재생              | 재생바 조작 시 리로드                                           | 재생바 조작 시 정상 수행                 |

## 트러블 슈팅

##### 증상

- MistServer가 MP4 파일의 스트림 데이터를 초기화하지 못함.
- 타임스탬프 동기화 문제 발생

##### 해결 방법

- FFmpeg를 사용하여 MP4 파일을 TS 형식으로 변환
- 변환된 TS 파일을 MistServer에 등록
