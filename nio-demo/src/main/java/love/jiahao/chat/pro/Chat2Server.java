package love.jiahao.chat.pro;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

/**
 * <big>改良版聊天室服务器 可以主动发消息</big>
 *
 * @author 13684
 * @date 2024/10/30
 */
public class Chat2Server {
    private Selector selector;
    private ServerSocketChannel serverSocketChannel;
    private final int PORT = 8080;
    private CopyOnWriteArrayList<SocketChannel> clients;

    public Chat2Server() {
        try {
            selector = Selector.open();
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.bind(new InetSocketAddress(PORT));
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            clients = new CopyOnWriteArrayList<>();
            System.out.println("服务器启动成功！");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void start() {
        Thread sendMessageThread = new Thread(() -> {
            try (
                    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))
            ) {
                while (true) {
                    System.out.println("请输入服务端消息：");
                    String message = reader.readLine();
                    for (SocketChannel client : clients) {
                        if (client != null && client.isConnected()) {
                            client.write(ByteBuffer.wrap((message + "\r\n").getBytes()));
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        sendMessageThread.start();

        while (true) {
            try {
                int readyChannels = selector.select();
                if (readyChannels == 0) {
                    continue;
                }
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = selectionKeys.iterator();
                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    keyIterator.remove();
                    handleKey(key);
                }


            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void handleKey(SelectionKey key) throws IOException {
        if (key.isAcceptable()) {
            SocketChannel socketChannel = serverSocketChannel.accept();
            socketChannel.configureBlocking(false);
            socketChannel.register(selector, SelectionKey.OP_READ);
            clients.add(socketChannel);
            System.out.println("客户端成功连接：" + socketChannel.getRemoteAddress());
        } else if (key.isReadable()) {
            SocketChannel socketChannel = (SocketChannel) key.channel();
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            int len;
            while ((len = socketChannel.read(buffer)) > 0) {
                buffer.flip();
                String msg = new String(buffer.array(), 0, len);
                if ("CLIENT_DISCONNECT\r\n".equals(msg)) {
                    System.out.println("客户端断开连接：" + socketChannel.getRemoteAddress());
                    key.cancel();
                    socketChannel.close();
                    clients.remove(socketChannel);
                    break;
                }else {
                    System.out.println(socketChannel.getRemoteAddress() + " 客户端说：" + msg);
                }
            }

            if (len == -1) {
                key.cancel();
                socketChannel.close();
                clients.remove(socketChannel);
                System.out.println("客户端异常断开连接：" + socketChannel.getRemoteAddress());
            }

        }
    }

    public static void main(String[] args) {
        new Chat2Server().start();
    }
}
