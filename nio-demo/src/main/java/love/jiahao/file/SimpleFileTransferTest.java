package love.jiahao.file;

import love.jiahao.common.constants.FileConstants;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * <big>复制文件对比</big>
 *
 * @author 13684
 * @date 2024/10/30
 */
public class SimpleFileTransferTest {
    /**
     * 计时
     *
     * @param task 不同的复制任务
     * @return 耗费时间
     */
    private static long measureTime(Runnable task) {
        long startTime = System.currentTimeMillis();
        task.run();
        long endTime = System.currentTimeMillis();
        return endTime - startTime;
    }

    /**
     * 使用传统的IO方法传输文件
     *
     * @param source 源文件
     * @param des    复制后的文件
     * @return 运行时间
     */
    private static long transferFile(File source, File des) {
        return measureTime(() -> {
            try (
                    BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(source));
                    BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(des));
            ) {
                byte[] bytes = new byte[1024 * 1024];
                int len;
                while ((len = inputStream.read(bytes)) != -1) {
                    outputStream.write(bytes, 0, len);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * 使用NIO的方式复制文件
     *
     * @param source 源文件
     * @param des    目标文件
     * @return 所用时间
     * @throws IOException 异常
     */
    private static long transferFileWithNIO(File source, File des) throws IOException {
        return measureTime(() -> {
            try (
                    RandomAccessFile read = new RandomAccessFile(source, "r");
                    RandomAccessFile write = new RandomAccessFile(des, "rw");
            ) {
                FileChannel readChannel = read.getChannel();
                FileChannel writeChannel = write.getChannel();

                ByteBuffer buffer = ByteBuffer.allocate(1024 * 1024);
                while (readChannel.read(buffer) > 0) {
                    buffer.flip();
                    writeChannel.write(buffer);
                    buffer.clear();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * 使用NIO中的transferTo方法将文件从源复制到目标
     * 此方法效率高，因为它直接在内核空间完成数据传输，避免了用户空间与内核空间的数据拷贝
     *
     * @param source 源文件
     * @param des 目标文件
     * @return 复制操作花费的时间
     */
    private static long nioTransferTo(File source, File des) {
        return measureTime(() -> {
            try (
                    // 打开源文件以读取
                    FileChannel sourceChannel = new RandomAccessFile(source, "r").getChannel();
                    // 打开目标文件以写入
                    FileChannel targetChannel = new RandomAccessFile(des, "rw").getChannel()
            ) {
                // 从源文件通道传输数据到目标文件通道
                long transferredBytes = sourceChannel.transferTo(0, sourceChannel.size(), targetChannel);
            } catch (IOException e) {
                // 如果发生IO异常，将其包装为运行时异常
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * 使用NIO中的transferFrom方法将文件从源复制到目标
     * 此方法同样高效，因为它直接在内核空间完成数据传输
     *
     * @param source 源文件
     * @param des 目标文件
     * @return 复制操作花费的时间
     */
    private static long nioTransferFrom(File source, File des) {
        return measureTime(() -> {
            try (FileChannel sourceChannel = new RandomAccessFile(source, "r").getChannel();
                 FileChannel targetChannel = new RandomAccessFile(des, "rw").getChannel()) {
                // 从源文件通道传输数据到目标文件通道
                long transferredBytes = targetChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
            } catch (IOException e) {
                // 打印堆栈跟踪信息
                e.printStackTrace();
            }
        });
    }

    /**
     * 使用NIO的内存映射文件方法将文件从源复制到目标
     * 此方法通过将文件映射到内存，然后从内存写入目标文件，避免了传统的读写操作
     *
     * @param source 源文件
     * @param des 目标文件
     * @return 复制操作花费的时间
     */
    private static long nioMap(File source, File des) {
        return measureTime(() -> {
            try (FileChannel sourceChannel = new RandomAccessFile(source, "r").getChannel();
                 FileChannel targetChannel = new RandomAccessFile(des, "rw").getChannel()) {
                long fileSize = sourceChannel.size();
                // 将源文件映射到内存
                MappedByteBuffer buffer = sourceChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileSize);
                // 将映射的数据写入目标文件
                targetChannel.write(buffer);
            } catch (IOException e) {
                // 打印堆栈跟踪信息
                e.printStackTrace();
            }
        });
    }

    /**
     * 程序入口点
     * 测试不同文件复制方法的性能
     *
     * @param args 命令行参数
     * @throws IOException 如果文件操作发生错误
     */
    public static void main(String[] args) throws IOException {
        // 定义源文件和目标文件路径
        File source = new File(FileConstants.COPY_PATH + "电子暗黑终结龙.jpg");
        File des = new File(FileConstants.COPY_PATH + "电子暗黑终结龙-IO.jpg");
        File nio = new File(FileConstants.COPY_PATH + "电子暗黑终结龙-NIO.jpg");
        File transferTo = new File(FileConstants.COPY_PATH + "电子暗黑终结龙-transferTo.jpg");
        File transferFrom = new File(FileConstants.COPY_PATH + "电子暗黑终结龙-transferFrom.jpg");
        File nioMap = new File(FileConstants.COPY_PATH + "电子暗黑终结龙-nioMap.jpg");

        // 执行并打印不同文件复制方法所花费的时间
        System.out.println("普通字节流：" + transferFile(source, des));
        System.out.println("NIO传输：" + transferFileWithNIO(source, nio));
        System.out.println("TransferTo传输：" + nioTransferTo(source, transferTo));
        System.out.println("TransferFrom传输：" + nioTransferFrom(source, transferFrom));
        System.out.println("NIOMap传输：" + nioTransferFrom(source, nioMap));
    }
}
