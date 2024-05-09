package com.infilos.spring.track;

import com.infilos.spring.track.api.Consts;
import kong.unirest.Config;
import kong.unirest.HttpRequest;
import kong.unirest.Interceptor;
import org.slf4j.MDC;

public class TrackUnirestMDCInterceptor implements Interceptor {

    @Override
    public void onRequest(HttpRequest<?> request, Config config) {
        String reqid = MDC.get(Consts.ReqidHeader);
        String corrid = MDC.get(Consts.CoridHeader);

        if (reqid != null) {
            request.headerReplace(Consts.ReqidHeader, reqid);
        }
        if (corrid != null) {
            request.headerReplace(Consts.CoridHeader, corrid);
        }
    }
}
