package com.lyz.auth.common.codec.handler.req;

import com.lyz.auth.common.codec.constant.AuthSerializable;
import com.lyz.auth.common.codec.constant.ReqType;
import lombok.Getter;

import java.util.EnumMap;
import java.util.Map;

/**
 * Desc:
 *
 * @author lyz
 * @version 1.0.0
 * @date 2024/4/23 14:43
 */
public abstract class AbstractReqTypeService implements ReqTypeService {

    private static final Map<ReqType, ReqTypeService> REQ_SERVICE_MAP = new EnumMap<>(ReqType.class);

    @Getter
    private final AuthSerializable serializable;

    public static ReqTypeService getReqTypeService(final ReqType reqType) {
        return REQ_SERVICE_MAP.get(reqType);
    }

    public AbstractReqTypeService() {
        ReqType reqType = getReqType();
        if (reqType == null) {
            throw new NullPointerException("ReqType is null");
        }
        REQ_SERVICE_MAP.put(getReqType(), this);
        serializable = AuthSerializable.getByCode(Byte.valueOf(System.getProperty("auth.netty.serializable", "0")).byteValue());
    }

    protected abstract ReqType getReqType();
}
