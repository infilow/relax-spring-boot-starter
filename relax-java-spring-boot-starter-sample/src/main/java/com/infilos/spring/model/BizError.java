package com.infilos.spring.model;

import com.infilos.spring.utils.RespondEnum;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum BizError implements RespondEnum<BizError> {
    PARAMS_INVALID(10001, "请求参数无效"),
    ENTITY_NOTFOUND(10002, "请求实体不存在: %s"),
    ;

    private final int code;
    private final String message;

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
