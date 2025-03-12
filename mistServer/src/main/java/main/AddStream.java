package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;

import com.google.gson.Gson;

public class AddStream {

    // 설정 정보
	private static final String MIST_API_URL = "http://<IP Address>:4242/api/addstream";
    private static final String MIST_USERNAME = "<MistServer ID>";
    private static final String MIST_PASSWORD = "<MistServer PW>";
    private static final String WATCH_DIR = "<watch path>"; // 감시 디렉토리 (윈도우)
    private static final String TARGET_DIR = "<target path>"; // 타겟 디렉토리 (리눅스)

    private static final String SFTP_HOST = "<linux address>";
    private static final int SFTP_PORT = 22;
    private static final String SFTP_USERNAME = "<linux User>"; 
    private static final String SFTP_PASSWORD = "<linux PW>";

    public static void main(String[] args) {
        try {
            WatchService watchService = FileSystems.getDefault().newWatchService();
            Path path = Paths.get(WATCH_DIR);
            path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);

            System.out.println("Monitoring directory: " + WATCH_DIR);

            while (true) {
                WatchKey key = watchService.take(); // 이벤트 대기
                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();

                    if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                        Path newFilePath = path.resolve((Path) event.context());
                        if (newFilePath.toString().endsWith(".mp4")) {
                            System.out.println("New MP4 file detected: " + newFilePath);
                            processNewFile(newFilePath.toString(), newFilePath.getFileName().toString());
                        }
                    }
                }
                key.reset(); // 다음 이벤트를 위해 키 리셋
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // MP4 파일 처리 함수
    private static void processNewFile(String sourcePath, String filename) {
        try {
            String remoteFilePath = uploadFileToServer(sourcePath, filename); // SFTP 업로드
            addStreamToMistServer(remoteFilePath, filename); // MistServer 스트림 추가
        } catch (Exception e) {
            System.err.println("Failed to process file: " + e.getMessage());
        }
    }

    // SFTP 연결 및 파일 업로드
    private static String uploadFileToServer(String sourcePath, String filename) throws Exception {
        try (SftpConnection sftpConnection = new SftpConnection(SFTP_HOST, SFTP_PORT, SFTP_USERNAME, SFTP_PASSWORD)) {
            String remoteFilePath = TARGET_DIR + "/" + filename;
            sftpConnection.upload(sourcePath, remoteFilePath);
            System.out.println("File uploaded successfully: " + remoteFilePath);
            return remoteFilePath;
        }
    }

    // MistServer에 스트림 추가
    private static void addStreamToMistServer(String remoteFilePath, String filename) throws Exception {
        // Challenge 요청 및 값 추출
        String challenge = getChallenge();

        // MD5 해시 계산
        String hashedPassword = DigestUtils.md5Hex(DigestUtils.md5Hex(MIST_PASSWORD) + challenge);

        // JSON 데이터 생성
        Gson gson = new Gson();
        Map<String, Object> payloadMap = createPayload(remoteFilePath, filename, hashedPassword);
        String payload = gson.toJson(payloadMap);

        // 스트림 추가 요청 전송
        HttpURLConnection connection = createHttpConnection(MIST_API_URL);
        sendHttpRequest(connection, payload);

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            System.out.println("Stream added successfully: " + sanitizeStreamName(filename));
        } else {
            throw new Exception("Failed to add stream to MistServer");
        }
    }

    // Challenge 요청 및 값 추출
    private static String getChallenge() throws Exception {
        HttpURLConnection connection = createHttpConnection(MIST_API_URL);
        sendHttpRequest(connection, "{\"command\":{}}");

        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new Exception("Failed to get challenge from MistServer");
        }

        return extractChallenge(connection);
    }

    // HTTP 연결 생성
    private static HttpURLConnection createHttpConnection(String url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);
        return connection;
    }

    // HTTP 요청 전송
    private static void sendHttpRequest(HttpURLConnection connection, String payload) throws IOException {
        try (OutputStream os = connection.getOutputStream()) {
            os.write(payload.getBytes());
        }
    }

    // JSON 데이터 생성
    private static Map<String, Object> createPayload(String remoteFilePath, String filename, String hashedPassword) {
        Map<String, Object> addstream = new HashMap<>();
        Map<String, String> streamDetails = new HashMap<>();
        streamDetails.put("name", sanitizeStreamName(filename));
        streamDetails.put("source", remoteFilePath);
        
        addstream.put(sanitizeStreamName(filename), streamDetails);

        Map<String, Object> payloadMap = new HashMap<>();
        payloadMap.put("addstream", addstream);

        Map<String, String> authorize = new HashMap<>();
        authorize.put("username", MIST_USERNAME);
        authorize.put("password", hashedPassword);

        payloadMap.put("authorize", authorize);
        
        return payloadMap;
    }

    // Challenge 값 추출 함수
    private static String extractChallenge(HttpURLConnection connection) throws Exception {
        try (InputStream is = connection.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {

            StringBuilder responseBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                responseBuilder.append(line);
            }

            String responseString = responseBuilder.toString();

            int challengeIndexStart = responseString.indexOf("\"challenge\":\"") + 13;
            int challengeIndexEnd = responseString.indexOf("\"", challengeIndexStart);

            return responseString.substring(challengeIndexStart, challengeIndexEnd);
        }
    }

    // 스트림 이름 정리 함수
    private static String sanitizeStreamName(String filename) {
        return filename.replace(".mp4", "").replaceAll("[^a-zA-Z0-9_+]", "_").concat("mp4");
    }

}