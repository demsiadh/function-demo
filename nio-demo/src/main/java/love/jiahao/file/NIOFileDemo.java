package love.jiahao.file;

import love.jiahao.common.constants.FileConstants;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.EnumSet;

/**
 * <big>NIO实现文件的写入和读取</big>
 *
 * @author 13684
 * @date 2024/10/30
 */
public class NIOFileDemo {
    static String fileName = FileConstants.BASE_PATH + "nio_test.txt";

    public static void main(String[] args) {
        writeFile();
        readFile();
    }

    private static void writeFile() {
        try (
                FileChannel fileChannel = FileChannel.open(Paths.get(fileName), EnumSet.of(StandardOpenOption.CREATE, StandardOpenOption.WRITE));
        ) {
            ByteBuffer buffer = ByteBuffer.wrap("零基础 学Java\r\n有基础 学Java".getBytes());
            fileChannel.write(buffer);
            System.out.println("写入完成");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // 使用 NIO 读取文件
    public static void readFile() {
        try (
                FileChannel fileChannel = FileChannel.open(Paths.get(fileName), StandardOpenOption.READ);
        ) {
            ByteBuffer buffer = ByteBuffer.allocate(1024);

            int bytesRead;
            while ((bytesRead = fileChannel.read(buffer)) != -1) {
                buffer.flip();
                System.out.println("读取到" + bytesRead + "字节" + " 内容: " + StandardCharsets.UTF_8.decode(buffer));
                buffer.clear();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
