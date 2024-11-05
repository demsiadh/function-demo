package love.jiahao.file;

import love.jiahao.common.constants.FileConstants;

import java.io.*;

/**
 * <big>BIO实现文件读取和写入</big>
 *
 * @author 13684
 * @date 2024/10/30
 */
public class BIOFileDemo {
    static String fileName = FileConstants.BASE_PATH + "bio_test.txt";

    public static void main(String[] args) {
        writeFile();
        readFile();
    }

    private static void writeFile() {
        try (
                BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(fileName));
        ) {
            bufferedWriter.write("零基础 学Java");
            bufferedWriter.newLine();
            bufferedWriter.write("有基础 学Java");
            bufferedWriter.newLine();
            System.out.println("写入完成");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void readFile() {
        try (
                BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName));
        ) {
            int no = 1;
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                System.out.println("第" + no++ + "行的读取内容：" + line);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
