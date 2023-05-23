package com.lyz.auth.common.netty.message;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lyz.auth.common.netty.constant.Serializable;
import com.lyz.auth.common.netty.util.JsonMapperUtil;
import lombok.Getter;
import lombok.Setter;
import org.msgpack.MessagePack;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * Desc:
 *
 * @author lyz
 * @version 1.0.0
 * @date 2023/3/21 15:11
 */
@Getter
@Setter
public class NettyMessage {

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
     * 编码header
     *
     * @return
     */
    public ByteBuffer encodeHeader() {
        return encodeHeader(this.body != null ? bodyEncode().length : 0);
    }

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
        return header.getSerializable() == Serializable.JAVA.getCode() ?  this.pack(header): JsonMapperUtil.toJSONString(header).getBytes(StandardCharsets.UTF_8);
    }

    /**
     * body encode
     *
     * @return
     */
    public byte[] bodyEncode() {
        if (body == null) {
            return null;
        }
        return header.getSerializable() == Serializable.JAVA.getCode() ?  this.pack(body): JsonMapperUtil.toJSONString(body).getBytes(StandardCharsets.UTF_8);
    }

    /**
     * put serializable type header length first byte
     *
     * @param source
     * @param serializable
     * @return
     */
    public static byte[] markProtocolType(int source, byte serializable) {
        byte[] result = new byte[]{serializable, (byte)(source >> 16 & 0xff), (byte)(source >> 8 & 0xff), (byte)(source & 0xff)};
        return result;
    }

    /**
     * decode
     *
     * @param buffer
     * @return
     */
    public static NettyMessage decode(ByteBuffer buffer) {
        int length = buffer.limit();
        //ori header length
        int oriHeaderLen = buffer.getInt();
        Serializable serializable = Serializable.getByCode((byte)(oriHeaderLen >> 24 & 0xff));
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
        NettyMessage message = new NettyMessage();
        message.setLength(length);
        message.setHeader(msgDecode(headerByte, serializable, MsgHeader.class));
        message.setBody(msgDecode(bodyByte, serializable, MsgBody.class));
        return message;
    }

    private static <T> T msgDecode(byte[] dataBytes, Serializable serializable, Class<T> tClass) {
        if (dataBytes == null) {
            return null;
        }
        switch (serializable) {
            case JAVA:
                return read(dataBytes, tClass);
            case JSON:
                return JsonMapperUtil.readValue(new String(dataBytes, StandardCharsets.UTF_8), tClass);
            case PROTOBUF:
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
}
