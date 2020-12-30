package com.infilos.spring.track.utils;

import com.infilos.spring.track.api.Consts;
import org.slf4j.MDC;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

public final class UuidGenerator {

    public void fillMdc(HttpServletRequest request) {
        MDC.clear();
        String reqid = request.getHeader(Consts.ReqidHeader);
        String corrid = request.getHeader(Consts.CoridHeader);

        if (reqid == null) {
            reqid = UUID.randomUUID().toString().replace("-", "");
        }
        if (corrid == null) {
            corrid = UUID.randomUUID().toString().replace("-", "");
        }

        MDC.put(Consts.ReqidHeader, reqid);
        MDC.put(Consts.CoridHeader, corrid);
    }
}
