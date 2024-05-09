package com.infilos.spring.track.utils;

import com.infilos.spring.track.api.Consts;
import org.slf4j.MDC;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

public final class UuidGenerator {

    public void fillMdc(HttpServletRequest request) {
        MDC.clear();
        String reqid = request.getHeader(Consts.ReqidHeader);
        String corrid = request.getHeader(Consts.CoridHeader);

        if (!StringUtils.hasText(reqid)) {
            reqid = UUID.randomUUID().toString().replace("-", "");
        }
        if (!StringUtils.hasText(corrid)) {
            corrid = UUID.randomUUID().toString().replace("-", "");
        }

        MDC.put(Consts.ReqidHeader, reqid);
        MDC.put(Consts.CoridHeader, corrid);
    }
}
