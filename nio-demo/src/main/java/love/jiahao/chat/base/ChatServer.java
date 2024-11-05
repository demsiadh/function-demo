package love.jiahao.chat.base;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;

/**
 * <big>聊天室服务端</big>
 * <p>只能回复客户端传入的消息，不能主动输入</p>
 *
 * @author 13684
 * @date 2024/10/30
 */
public class ChatServer {
    private final Selector selector;
    private final ServerSocketChannel serverSocketChannel;
    private static final int PORT = 8080;

    public ChatServer() {
        try {
            // 获取选择器
            selector = Selector.open();
            // 获取套接字监听通道
            serverSocketChannel = ServerSocketChannel.open();
            // 绑定端口号
            serverSocketChannel.socket().bind(new InetSocketAddress(PORT));
            // 设置为非阻塞
            serverSocketChannel.configureBlocking(false);
            // 注册选择器，监听accept
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("服务器启动成功! " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void start() {
        while (true) {
            try {
                if (selector.select() > 0) {
                    Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
                    while (keyIterator.hasNext()) {
                        SelectionKey key = keyIterator.next();
                        keyIterator.remove();
                        handleKey(key);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleKey(SelectionKey key) throws IOException {
        if (key.isAcceptable()) {
            SocketChannel socketChannel = serverSocketChannel.accept();
            socketChannel.configureBlocking(false);
            socketChannel.register(selector, SelectionKey.OP_READ);
            System.out.println("客户端成功连接：" + socketChannel.getRemoteAddress());
        } else if (key.isReadable()) {
            SocketChannel socketChannel = (SocketChannel) key.channel();
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            int len;
            while ((len = socketChannel.read(buffer)) > 0) {
                buffer.flip();
                String msg = new String(buffer.array(), 0, len);
                System.out.println("客户端说：" + msg);
                socketChannel.write(ByteBuffer.wrap(("服务端回复：" + msg).getBytes()));
            }
        }
    }

    public static void main(String[] args) {
        new ChatServer().start();
    }
}
