package love.jiahao.common.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * <big>关于io的工具类</big>
 *
 * @author 13684
 * @date 2024/10/15
 */
public class IOUtil {

    /**
     * 将InputStream转换为字节数组
     * 这个方法主要用于读取InputStream的内容，并将其转换为字节数组
     * 之所以使用BufferedInputStream和ByteArrayOutputStream，是为了提高读取效率和减少内存消耗
     *
     * @param inputStream 输入流，可以是文件流、网络流等
     * @return 字节数组，包含InputStream的全部内容
     * @throws IOException 如果在读取过程中发生I/O错误，将抛出此异常
     */
    public static byte[] inputStreamToByteArray(InputStream inputStream) throws IOException {
        // 使用BufferedInputStream包装输入流，以提高读取效率
        BufferedInputStream bis = new BufferedInputStream(inputStream);
        // 使用ByteArrayOutputStream来存储读取到的字节
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        // 用于存储每次读取到的字节数
        int nRead;
        // 定义字节数组，用于从输入流读取数据
        byte[] data = new byte[1024];
        // 循环读取输入流中的数据，直到末尾
        while ((nRead = bis.read(data, 0, data.length)) != -1) {
            // 将读取到的数据写入ByteArrayOutputStream中
            buffer.write(data, 0, nRead);
        }
        // 刷新输出流，确保所有数据都被写入到ByteArrayOutputStream中
        buffer.flush();
        // 返回包含InputStream全部内容的字节数组
        return buffer.toByteArray();
    }
}
