package com.lyz.auth.common.netty.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * DESC:
 *
 * @author lyz
 * @version 1.0.0
 * @date 2023/3/20 0:05
 */
@AllArgsConstructor
public enum Serializable {
    JAVA((byte) 0, "Java序列化"),
    JSON((byte) 1, "Json序列化"),
    PROTOBUF((byte) 2, "Protobuf序列化"),
    ;

    @Getter
    private byte code;
    @Getter
    private String desc;

    public static Serializable getByCode(byte code) {
        for (Serializable serializable : Serializable.values()) {
            if (code == serializable.code) {
                return serializable;
            }
        }
        return null;
    }
}
