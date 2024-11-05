package love.jiahao.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * <big>传统的IO服务器</big>
 *
 * @author 13684
 * @date 2024/10/30
 */
public class IOServer {
    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(8080);
            while (true) {
                try (
                        Socket client = serverSocket.accept();
                        InputStream inputStream = client.getInputStream();
                        OutputStream outputStream = client.getOutputStream();
                ) {
                    byte[] bytes = new byte[1024];

                    int readLen = inputStream.read(bytes);
                    System.out.println("收到数据：" + new String(bytes, 0, readLen));
                    outputStream.write(bytes, 0, readLen);
                }


            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
