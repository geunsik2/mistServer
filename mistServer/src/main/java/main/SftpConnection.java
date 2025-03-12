package main;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class SftpConnection implements AutoCloseable {

    private Session session;
    private ChannelSftp channelSftp;

    // 생성자: SFTP 연결 초기화
    public SftpConnection(String host, int port, String username, String password) throws Exception {
        JSch jsch = new JSch();
        session = jsch.getSession(username, host, port);
        session.setPassword(password);
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect();

        channelSftp = (ChannelSftp) session.openChannel("sftp");
        channelSftp.connect();
    }

    // 파일 업로드 메서드
    public void upload(String localPath, String remotePath) throws Exception {
        channelSftp.put(localPath, remotePath);
    }

    // 자원 정리 (AutoCloseable 구현)
    @Override
    public void close() throws Exception {
        if (channelSftp != null && channelSftp.isConnected()) {
            channelSftp.disconnect();
        }
        if (session != null && session.isConnected()) {
            session.disconnect();
        }
    }
}