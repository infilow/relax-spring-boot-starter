package com.infilos.spring.track;

import com.infilos.spring.track.api.Consts;
import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

public final class TrackSupport {
    private TrackSupport() {
    }

    private static final InheritableThreadLocal<Map<String, String>> CURRENT_THREAD_LOCAL = new InheritableThreadLocal<>();

    public static String getThreadValue(String key) {
        return CURRENT_THREAD_LOCAL.get().get(key);
    }

    public static void setThreadValue(String key, String value) {
        CURRENT_THREAD_LOCAL.get().put(key, value);
    }

    public static void setThreadValue(Map<String, String> map) {
        CURRENT_THREAD_LOCAL.set(map);
    }

    public static void clearThreadValue() {
        CURRENT_THREAD_LOCAL.get().clear();
        CURRENT_THREAD_LOCAL.remove();
    }

    public static <T> Callable<T> wrapMDCContext(Callable<T> task) {
        Map<String, String> contextMap = MDC.getCopyOfContextMap();
        return () -> {
            setMDCContext(contextMap);
            try {
                return task.call();
            } finally {
                MDC.clear();
            }
        };
    }

    public static Runnable wrapMDCContext(Runnable task) {
        Map<String, String> contextMap = MDC.getCopyOfContextMap();
        return () -> {
            setMDCContext(contextMap);
            try {
                task.run();
            } finally {
                MDC.clear();
            }
        };
    }

    public static void setMDCContext(Map<String, String> contextMap) {
        MDC.clear();
        if (contextMap != null) {
            MDC.setContextMap(contextMap);
        }
    }

    public static void fillMDCContext(HttpServletRequest request) {
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

    public static void clearMDCContext() {
        MDC.clear();
    }

    public static void fillThreadMDCContext() {
        setThreadValue(MDC.getCopyOfContextMap());
    }

    public static void clearThreadMDCContext() {
        clearThreadValue();
    }

    /**
     * Spring {@link org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor#setTaskDecorator(TaskDecorator)} for @Async method tracing
     * </p>
     * Or, Spring {@link org.springframework.boot.task.TaskExecutorBuilder#taskDecorator(TaskDecorator)} for @Async method tracing
     */
    public static TaskDecorator getMDCContextTaskDecorator() {
        return TrackSupport::wrapMDCContext;
    }
}
