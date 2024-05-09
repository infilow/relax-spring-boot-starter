package com.infilos.spring.rest;

import com.infilos.spring.model.BizError;
import com.infilos.spring.service.DemoAsyncService;
import com.infilos.spring.track.TrackSupport;
import com.infilos.spring.track.concurrent.MDCAwareThreadPoolExecutor;
import com.infilos.spring.utils.Respond;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.*;

@Slf4j
@Tag(name = "错误接口")
@RestController
@RequestMapping("/error")
public class ErrorController {

    private static final ExecutorService PLAIN_EXECUTOR = Executors.newCachedThreadPool();
    private static final ThreadPoolExecutor MDC_EXECUTOR = new MDCAwareThreadPoolExecutor(
        4, 4, 2, TimeUnit.MINUTES,
        new SynchronousQueue<>(),
        Executors.defaultThreadFactory(),
        new ThreadPoolExecutor.AbortPolicy()
    );


    @Autowired
    private DemoAsyncService demoAsyncService;

    @GetMapping("/param/invaild")
    public Respond<Void> mockParamInvaild() {
        return Respond.failed(BizError.PARAMS_INVALID);
    }

    @GetMapping("/entity/notfound")
    public Respond<Void> mockEntityNotFound(@RequestParam Long id) throws ExecutionException, InterruptedException {
        log.info("Process mockEntityNotFound request...");

        new Thread() {
            @Override
            public void run() {
                log.info("Process mockEntityNotFound in thread pool 0...");
            }
        }.join();

        Future<Integer> result1 = PLAIN_EXECUTOR.submit(TrackSupport.wrapMDCContext(() -> {
            log.info("Process mockEntityNotFound in thread pool 1...");
            return 0;
        }));
        result1.get();

        Future<Integer> result2 = PLAIN_EXECUTOR.submit(TrackSupport.wrapMDCContext(() -> {
            log.info("Process mockEntityNotFound in thread pool 2...");
            return 0;
        }));
        result2.get();

        CompletableFuture<Void> result3 = CompletableFuture.runAsync(TrackSupport.wrapMDCContext(() -> {
            log.info("Process mockEntityNotFound in thread pool 3...");
        }), PLAIN_EXECUTOR);
        result3.join();

        CompletableFuture<Integer> result4 = CompletableFuture.supplyAsync(() -> {
            log.info("Process mockEntityNotFound in thread pool 4...");
            return 4;
        }, MDC_EXECUTOR);
        CompletableFuture<Integer> result5 = CompletableFuture.supplyAsync(() -> {
            log.info("Process mockEntityNotFound in thread pool 5...");
            return 5;
        }, MDC_EXECUTOR);
        result4.join();
        result5.join();

        // spring async method call
        demoAsyncService.processAsync(1);

        // invoke http with unirest
        demoAsyncService.invokeHttpApi();

        return Respond.failed(BizError.ENTITY_NOTFOUND, id);
    }
}
