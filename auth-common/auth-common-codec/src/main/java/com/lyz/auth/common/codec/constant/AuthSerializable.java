package com.lyz.auth.common.codec.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Desc:自定义序列化方式枚举
 *
 * @author lyz
 * @version 1.0.0
 * @date 2024/4/23 11:14
 */
@Getter
@AllArgsConstructor
public enum AuthSerializable {
    JAVA((byte) 0, "Java序列化"),
    JSON((byte) 1, "Json序列化"),
    ;

    private final byte code;
    private final String desc;

    public static AuthSerializable getByCode(byte code) {
        for (AuthSerializable serializable : AuthSerializable.values()) {
            if (code == serializable.code) {
                return serializable;
            }
        }
        return null;
    }
}
