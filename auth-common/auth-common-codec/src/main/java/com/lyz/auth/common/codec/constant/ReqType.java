package com.lyz.auth.common.codec.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * DESC:请求类型
 *
 * @author lyz
 * @version 1.0.0
 * @date 2023/3/20 0:01
 */
@Getter
@AllArgsConstructor
public enum ReqType {
    REQ((byte) 0, "请求报文"),
    RES((byte) 1, "返回报文"),
    PING((byte) 2, "心跳ping报文"),
    PONG((byte) 3, "心跳pong报文"),
    ;

    private final byte code;
    private final String desc;

    public static ReqType getByCode(byte code) {
        for (ReqType reqType : ReqType.values()) {
            if (code == reqType.code) {
                return reqType;
            }
        }
        return null;
    }
}
