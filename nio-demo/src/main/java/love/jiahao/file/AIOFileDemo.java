package love.jiahao.file;

import love.jiahao.common.constants.FileConstants;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * <big>AIO实现文件写入和读取</big>
 *
 * @author 13684
 * @date 2024/10/30
 */
public class AIOFileDemo {
    static String fileName = FileConstants.BASE_PATH + "aio_test.txt";

    public static void main(String[] args) {
        writeFile();
        readFile();
    }

    private static void writeFile() {
        try (
                AsynchronousFileChannel asynchronousFileChannel = AsynchronousFileChannel.open(Paths.get(fileName), StandardOpenOption.WRITE, StandardOpenOption.CREATE);
        ) {
            ByteBuffer buffer = ByteBuffer.wrap("零基础 学Java\r\n有基础 学Java".getBytes());

            // 异步写入
            Future<Integer> result = asynchronousFileChannel.write(buffer, 0);
            // 等待写操作完成
            result.get();

            System.out.println("写入完成");

        } catch (IOException | ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static void readFile() {
        try (
                AsynchronousFileChannel asynchronousFileChannel = AsynchronousFileChannel.open(Paths.get(fileName), StandardOpenOption.WRITE, StandardOpenOption.READ);
        ) {
            ByteBuffer buffer = ByteBuffer.allocate(1024);

            asynchronousFileChannel.read(buffer, 0, buffer, new CompletionHandler<Integer, ByteBuffer>() {
                @Override
                public void completed(Integer result, ByteBuffer attachment) {
                    if (result > 0) {
                        attachment.flip();
                        System.out.println("读取的内容：" + StandardCharsets.UTF_8.decode(attachment));
                        attachment.clear();
                    }
                }

                @Override
                public void failed(Throwable exc, ByteBuffer attachment) {
                    System.out.println("读取失败！");
                    exc.printStackTrace();
                }
            });
            // 等待异步操作完成
            Thread.sleep(1000);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
