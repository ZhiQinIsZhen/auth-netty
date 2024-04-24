package com.lyz.auth.common.util.result;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;

/**
 * Desc:
 *
 * @author lyz
 * @version 1.0.0
 * @date 2023/3/9 9:42
 */
@Getter
@Setter
@JsonPropertyOrder({"code", "message", "data"})
public class Result<T> {

    private String code;

    private String message;

    private T data;

    public Result(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public Result(T data) {
        this("0", "成功");
        this.data = data;
    }

    public static <E> Result<E> success(E data) {
        return new Result<>(data);
    }

    public static <E> Result<E> success() {
        return success(null);
    }

    public static <E> Result<E> error(String code, String message) {
        return new Result<>(code, message);
    }
}
