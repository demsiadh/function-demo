package love.jiahao.chat.pro;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * <big>客户端-聊天室</big>
 *
 * @author 13684
 * @date 2024/10/30
 */
public class Chat2Client {
    private Selector selector;
    private SocketChannel socketChannel;
    private static final String HOST = "localhost";
    private static final int PORT = 8080;

    public Chat2Client() {
        try {
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            socketChannel.connect(new InetSocketAddress(HOST, PORT));

            selector = Selector.open();
            socketChannel.register(selector, SelectionKey.OP_CONNECT);
            System.out.println("连接到聊天室了！");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        new Thread(() -> {
            try (
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in))
            ) {
              while (true) {
                  System.out.println("请输入客户端消息：");
                  String message = bufferedReader.readLine();
                  if ("T".equals(message)) {
                      // 发送特殊消息通知服务器客户端要断开连接
                      sendMessage("CLIENT_DISCONNECT");
                      socketChannel.close();
                      selector.close();
                      System.exit(0);
                  } else {
                      sendMessage(message);
                  }
              }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

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
                    if (key.isConnectable()) {
                        // 连接到服务器
                        socketChannel.finishConnect();
                        socketChannel.register(selector, SelectionKey.OP_READ);
                        System.out.println("已连接到服务器");
                    } else if (key.isReadable()) {
                        readMessage();
                    }
                    keyIterator.remove();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
    }

    private void sendMessage(String message) throws IOException {
        if (message != null && !message.trim().isEmpty() && socketChannel.isConnected()) {
            ByteBuffer buffer = ByteBuffer.wrap((message + "\r\n").getBytes());
            socketChannel.write(buffer);
        }
    }

    private void readMessage() throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int len;
        while ((len = socketChannel.read(buffer)) > 0) {
            buffer.flip();
            System.out.println("服务端消息：" + new String(buffer.array(), 0, len));
        }
    }

    public static void main(String[] args) {
        new Chat2Client().start();
    }
}
