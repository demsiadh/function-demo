package love.jiahao.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * <big>基于NIO实现的web服务器</big>
 *
 * @author 13684
 * @date 2024/10/30
 */
public class AIOServer {
    public static void main(String[] args) throws IOException, InterruptedException {
        try (
                AsynchronousServerSocketChannel serverSocketChannel = AsynchronousServerSocketChannel.open();
        ) {
            serverSocketChannel.bind(new InetSocketAddress("127.0.0.1", 8080));
            System.out.println("服务器启动成功! " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            serverSocketChannel.accept(buffer, new CompletionHandler<AsynchronousSocketChannel, ByteBuffer>() {
                @Override
                public void completed(AsynchronousSocketChannel client, ByteBuffer buffer) {
                    serverSocketChannel.accept(null, this);
                    Future<Integer> read = client.read(buffer);
                    try {
                        read.get();
                        buffer.flip();
                        System.out.println("接收到的消息：" + new String(buffer.array(), 0, buffer.remaining()));
                    } catch (InterruptedException | ExecutionException e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public void failed(Throwable exc, ByteBuffer attachment) {
                    exc.printStackTrace();
                }
            });
            Thread.currentThread().join();
        }


    }
}
