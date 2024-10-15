package love.jiahao.common.domain;

import lombok.Data;
import love.jiahao.common.constants.ResponseCode;
import love.jiahao.common.constants.ResponseMsg;


/**
 * <big>响应实体</big>
 *
 * @author 13684
 * @date 2024/10/13
 */
@Data
public class ResponseEntity<T> {
    // 响应状态码
    private int code;
    // 响应消息
    private String msg;
    // 响应数据
    private T data;

    /**
     * 创建一个表示成功的响应实体，不包含数据
     *
     * @return ResponseEntity<Void> 表示成功的响应实体，且不包含数据
     */
    public static ResponseEntity<Void> ok() {
        return new ResponseEntity<>(ResponseCode.SUCCESS, ResponseMsg.OK, null);
    }

    /**
     * 创建一个表示成功的响应实体，并包含指定的数据
     *
     * @param data 响应的数据
     * @param <T>  数据的类型
     * @return ResponseEntity<T> 表示成功的响应实体，包含指定的数据
     */
    public static <T> ResponseEntity<T> ok(T data) {
        return new ResponseEntity<T>(ResponseCode.SUCCESS, ResponseMsg.OK, data);
    }

    /**
     * 创建一个表示错误的响应实体，包含错误消息
     *
     * @param msg 错误消息
     * @param <T> 泛型参数，表示响应数据的类型
     * @return ResponseEntity<T> 表示错误的响应实体，不包含数据
     */
    public static <T> ResponseEntity<T> error(String msg) {
        return new ResponseEntity<T>(ResponseCode.ERROR, msg, null);
    }

    /**
     * 创建一个表示错误的响应实体，包含错误码和错误消息
     *
     * @param code 错误码
     * @param msg  错误消息
     * @param <T>  泛型参数，表示响应数据的类型
     * @return ResponseEntity<T> 表示错误的响应实体，不包含数据
     */
    public static <T> ResponseEntity<T> error(int code, String msg) {
        return new ResponseEntity<T>(code, msg, null);
    }

    /**
     * 构造方法，用于创建一个响应实体
     *
     * @param code 响应状态码
     * @param msg  响应消息
     * @param data 响应数据
     */
    public ResponseEntity(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }
}
