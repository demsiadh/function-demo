package love.jiahao.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * <big>基于NIO的TCP服务器</big>
 *
 * @author 13684
 * @date 2024/10/30
 */
public class NIOServer {
    public static void main(String[] args) throws IOException {
        // 创建一个服务器套接字通道，用于监听客户端的连接请求
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        // 设置服务器套接字为非阻塞
        serverSocketChannel.configureBlocking(false);
        // 绑定服务器至8080端口
        serverSocketChannel.socket().bind(new InetSocketAddress(8081));
        // 获取一个选择器
        Selector selector = Selector.open();
        // 将服务器套接字通道注册到选择器上，触发事件为客户端连接
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        // 死循环处理事件
        while (true) {
            // 获取准备好的通道数量
            int readyChannels = selector.select();

            // 如果没有就下一次循环
            if (readyChannels == 0) {
                continue;
            }
            // 获取选择器中所有准备好的通道的选择键集合
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            // 获取迭代器进行遍历
            Iterator<SelectionKey> keyIterator = selectionKeys.iterator();

            while (keyIterator.hasNext()) {
                // 获取当前选择键
                SelectionKey key = keyIterator.next();
                // 如果是新的连接
                if (key.isAcceptable()) {
                    // 获取服务器套接字通道
                    ServerSocketChannel server = (ServerSocketChannel) key.channel();
                    // 接受客户端连接，并返回客户端套接字
                    SocketChannel client = server.accept();
                    // 设置客户端套接字为非阻塞模式
                    client.configureBlocking(false);
                    // 将客户端连接注册到选择器上，关注读事件
                    client.register(selector, SelectionKey.OP_READ);
                    // 如果是通道可读
                } else if (key.isReadable()) {
                    // 获取通道
                    SocketChannel client = (SocketChannel) key.channel();
                    // 获取buffer
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    // 读取数据，并写入字符串中
                    int readBytes = client.read(buffer);
                    if (readBytes > 0) {
                        buffer.flip();
                        System.out.println("收到数据：" + new String(buffer.array(), 0, readBytes));
                        // 将连接注册到选择器，关注可写事件
                        client.register(selector, SelectionKey.OP_WRITE);
                    } else if (readBytes < 0) {
                        client.close();
                    }
                    // 如果是通道可写
                } else if (key.isWritable()) {
                    // 处理写事件
                    SocketChannel client = (SocketChannel) key.channel();
                    ByteBuffer buffer = ByteBuffer.wrap("Hello, Client!".getBytes());
                    client.write(buffer);

                    // 将客户端通道注册到 Selector 并监听 OP_READ 事件
                    client.register(selector, SelectionKey.OP_READ);
                    client.close();
                }
                // 处理完当前通道，移除，避免重复处理
                keyIterator.remove();
            }
        }
    }
}
