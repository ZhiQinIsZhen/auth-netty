package com.lyz.auth.common.codec;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lyz.auth.common.codec.constant.AuthSerializable;
import com.lyz.auth.common.util.JsonMapperUtil;
import lombok.Getter;
import lombok.Setter;
import org.msgpack.MessagePack;
import org.msgpack.annotation.Message;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * Desc:自定义socket消息体
 *
 * @author lyz
 * @version 1.0.0
 * @date 2024/4/23 11:01
 */
@Getter
@Setter
public class AuthNettyMsg {

    @JsonIgnore
    private static final MessagePack messagePack = new MessagePack();

    /**
     * 消息长度
     */
    private int length;

    /**
     * 消息头
     */
    private transient MsgHeader header;

    /**
     * 消息体
     */
    private transient MsgBody body;

    /**
     * 编码header信息
     *
     * @param bodyLength 长度
     * @return ByteBuffer
     */
    public ByteBuffer encodeHeader(int bodyLength) {
        //header length
        int length = 4;
        byte[] headerBytes = this.headerEncode();
        //add header
        length += headerBytes.length;
        //add body
        length += bodyLength;
        //length | header length | header | body
        ByteBuffer bb = ByteBuffer.allocate(4 + length - bodyLength);
        bb.putInt(length);
        bb.put(markProtocolType(headerBytes.length, header.getSerializable()));
        bb.put(headerBytes);
        bb.flip();
        return bb;
    }

    /**
     * header encode
     *
     * @return
     */
    public byte[] headerEncode() {
        return header.getSerializable() == AuthSerializable.JAVA.getCode() ?  this.pack(header): JsonMapperUtil.toJSONString(header).getBytes(StandardCharsets.UTF_8);
    }

    /**
     * 编码header
     *
     * @return ByteBuffer
     */
    public ByteBuffer encodeHeader() {
        return encodeHeader(this.body != null ? bodyEncode().length : 0);
    }

    /**
     * body encode
     *
     * @return byte[]
     */
    public byte[] bodyEncode() {
        if (body == null) {
            return null;
        }
        return header.getSerializable() == AuthSerializable.JAVA.getCode()
                ?  this.pack(body): JsonMapperUtil.toJSONString(body).getBytes(StandardCharsets.UTF_8);
    }

    /**
     * put serializable type header length first byte
     *
     * @param source 长度
     * @param serializable 协议类型
     * @return byte[]
     */
    public static byte[] markProtocolType(int source, byte serializable) {
        return new byte[]{serializable, (byte)(source >> 16 & 0xff), (byte)(source >> 8 & 0xff), (byte)(source & 0xff)};
    }

    /**
     * decode
     *
     * @param buffer buffer
     * @return AuthNettyMsg
     */
    public static AuthNettyMsg decode(ByteBuffer buffer) {
        int length = buffer.limit();
        //ori header length
        int oriHeaderLen = buffer.getInt();
        AuthSerializable serializable = AuthSerializable.getByCode((byte)(oriHeaderLen >> 24 & 0xff));
        int headerLength = oriHeaderLen & 0xffffff;
        //header
        byte[] headerByte = new byte[headerLength];
        buffer.get(headerByte);
        //body length
        int bodyLength = length - 4 - headerLength;
        byte[] bodyByte = null;
        if (bodyLength > 0) {
            bodyByte = new byte[bodyLength];
            buffer.get(bodyByte);
        }
        //result
        AuthNettyMsg message = new AuthNettyMsg();
        message.setLength(length);
        message.setHeader(msgDecode(headerByte, serializable, MsgHeader.class));
        message.setBody(msgDecode(bodyByte, serializable, MsgBody.class));
        return message;
    }

    private static <T> T msgDecode(byte[] dataBytes, AuthSerializable serializable, Class<T> tClass) {
        if (dataBytes == null) {
            return null;
        }
        switch (serializable) {
            case JAVA:
                return read(dataBytes, tClass);
            case JSON:
                return JsonMapperUtil.readValue(new String(dataBytes, StandardCharsets.UTF_8), tClass);
            default:
                return null;
        }
    }

    private <T> byte[] pack(T t) {
        try {
            return messagePack.write(t);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static <T> T read(byte[] bytes, Class<T> tClass) {
        if (bytes == null) {
            return null;
        }
        try {
            return messagePack.read(bytes, tClass);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Getter
    @Setter
    @Message
    public static class MsgHeader {

        /**
         * 版本
         */
        private int version;

        /**
         * 序列化方式
         */
        private byte serializable;

        /**
         * 请求类型
         */
        private byte reqType;
    }

    @Getter
    @Setter
    @Message
    public static class MsgBody {

        /**
         * 操作类型
         */
        private String op;

        /**
         * 具体参数
         */
        private String args;
    }
}
