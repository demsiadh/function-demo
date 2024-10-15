package love.jiahao.file.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import love.jiahao.common.domain.ResponseEntity;
import love.jiahao.common.util.IOUtil;
import love.jiahao.file.contants.FileConstants;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * <big>文件控制器</big>
 *
 * @author 13684
 * @date 2024/10/13
 */
@RestController
@RequestMapping("/file")
@Slf4j
@RequiredArgsConstructor
public class FileController {
    /**
     * 小文件上传
     *
     * @param file 文件
     * @return 响应结果
     */
    @PostMapping("/upload")
    public ResponseEntity<String> upload(@RequestBody MultipartFile file) throws IOException {
        // 获取上传文件的原始文件名
        String originalFilename = file.getOriginalFilename();
        // 提取文件名部分，去除路径信息
        String filename = StringUtils.getFilename(originalFilename);
        // 生成一个唯一格式的文件名，用于防止文件名冲突
        String format = String.format("%s.%s", UUID.randomUUID(), filename);
        // 创建目标文件对象，指定文件保存的目录和文件名
        File dstFile = new File(FileConstants.UPLOAD_PATH, format);
        // 将上传的临时文件转移到指定的目标文件位置
        file.transferTo(dstFile);
        // 返回文件的绝对路径作为响应体
        return ResponseEntity.ok(dstFile.getName());
    }

    /**
     * 上传大文件
     * 1. 前端进行分片，后端根据分片进行写入文件
     * 2. 采用一个conf文件记录各个分片上传情况
     * 3. 上传完后计算md5，是否和前端传入的一致
     *
     * @param chunkSize 分片大小
     * @param totalNumber 分片总个数
     * @param chunkNumber 当前分片序号
     * @param md5 总文件的md5值
     * @param file 当前分片文件
     * @return 上传后的路径
     * @throws IOException 异常
     */
    @PostMapping(value = "/uploadBig")
    public ResponseEntity<String> uploadBig(@RequestParam Long chunkSize,
                                            @RequestParam Integer totalNumber,
                                            @RequestParam Long chunkNumber,
                                            @RequestParam String md5,
                                            @RequestParam MultipartFile file) throws IOException {
        // 上传文件的路径
        String uploadPath = String.format("%s%s\\%s.%s", FileConstants.UPLOAD_PATH, md5, md5, StringUtils.getFilenameExtension(file.getOriginalFilename()));
        // 文件分片信息路径
        String confPath = String.format("%s%s\\%s.conf", FileConstants.UPLOAD_PATH, md5, md5);

        // 创建文件父目录
        File dir = new File(uploadPath).getParentFile();
        if (!dir.exists()) {
            // 创建目录
            dir.mkdir();
            // 所有分片状态设置为0
            byte[] bytes = new byte[totalNumber];
            // 写入文件中
            Files.write(Paths.get(confPath), bytes);
        }

        // 随机分片写入文件
        try (
                RandomAccessFile randomAccessFile = new RandomAccessFile(uploadPath, "rw");
                RandomAccessFile randomAccessConfFile = new RandomAccessFile(confPath, "rw");
                InputStream inputStream = file.getInputStream();
        ) {
            // 定位到当前分片的偏移量
            randomAccessFile.seek(chunkNumber * chunkSize);
            // 写入该分片的数据
            randomAccessFile.write(IOUtil.inputStreamToByteArray(inputStream));
            // 定位到该分片的状态位置
            randomAccessConfFile.seek(chunkNumber);
            // 设置状态为1（表示已完成）
            randomAccessConfFile.write(1);

        }
        return ResponseEntity.ok(uploadPath);
    }

    /**
     * 校验分片文件是否上传成功
     * 1. 如果conf全是1，并且md5与前端不一致就是上传失败了
     * 2. 如果conf不全是1，说明还没有上传成功，继续上传其他分片
     * 3. 目录不存在就是文件没有上传过
     *
     * @param md5 md5值
     * @return 是否上传成功
     * @throws IOException e
     */
    @GetMapping("/checkFile")
    public ResponseEntity<String> uploadBig(@RequestParam String md5) throws IOException {
        // 文件分片信息路径
        String confPath = String.format("%s%s\\%s.conf", FileConstants.UPLOAD_PATH, md5, md5);
        Path path = Paths.get(confPath);
        // MD5目录不存在文件从未上传过
        if (!Files.exists(path.getParent())) {
            return ResponseEntity.ok("文件未上传！");
        }
        // 判断文件是否上传成功
        StringBuilder stringBuilder = new StringBuilder();
        byte[] bytes = Files.readAllBytes(path);
        // 拼接到字符串中
        for (byte b : bytes) {
            stringBuilder.append(b);
        }
        // 所有分片上传完成计算文件的MD5
        if (!stringBuilder.toString().contains("0")) {
            File file = new File(String.format("%s%s\\", FileConstants.UPLOAD_PATH, md5));
            File[] files = file.listFiles();
            String filePath = "";
            assert files != null;
            for (File f : files) {
                if (!f.getName().contains("conf")) {
                    filePath = f.getAbsolutePath();
                    try (
                            InputStream inputStream = Files.newInputStream(f.toPath());
                    ) {
                        String md5pwd = DigestUtils.md5DigestAsHex(inputStream);
                        if (!md5pwd.equalsIgnoreCase(md5)) {
                            return ResponseEntity.ok("文件上传失败！");
                        }
                    }
                }
            }
            return ResponseEntity.ok(filePath);
        } else {
            return ResponseEntity.ok(stringBuilder.toString());
        }
    }


}
