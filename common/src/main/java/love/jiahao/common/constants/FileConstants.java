package love.jiahao.common.constants;

/**
 * <big>文件相关常量</big>
 *
 * @author 13684
 * @date 2024/10/30
 */
public interface FileConstants {
    // 基础路径
    String BASE_PATH = System.getProperty("user.dir") + "\\data\\";
    // 文件上传路径
    String UPLOAD_PATH = BASE_PATH + "upload\\";
    // 文件拷贝路径
    String COPY_PATH = BASE_PATH + "copy\\";
}
