//package com.infilos.spring.track.concurrent;
//
//import com.infilos.spring.track.TrackSupport;
//import org.slf4j.MDC;
//
//import java.util.Map;
//import java.util.concurrent.CompletableFuture;
//import java.util.concurrent.CompletionStage;
//import java.util.concurrent.Executor;
//import java.util.concurrent.ExecutorService;
//import java.util.function.Function;
//
//public class MDCAwareCompletableFuture<T> extends CompletableFuture<T> {
//    public static final ExecutorService MDC_AWARE_ASYNC_POOL = new MDCAwareForkJoinPool();
//
//    @Override
//    public <U> CompletableFuture<U> newIncompleteFuture() {
//        return new MDCAwareCompletableFuture<>();
//    }
//
//    @Override
//    public Executor defaultExecutor() {
//        return MDC_AWARE_ASYNC_POOL;
//    }
//
//    public static <T> CompletionStage<T> getMDCAwareCompletionStage(CompletableFuture<T> future) {
//        return new MDCAwareCompletableFuture<>()
//            .completeAsync(() -> null)
//            .thenCombineAsync(future, (aVoid, value) -> value);
//    }
//
//    public static <T> CompletionStage<T> getMDCHandledCompletionStage(CompletableFuture<T> future,
//                                                                      Function<Throwable, T> throwableFunction) {
//        Map<String, String> contextMap = MDC.getCopyOfContextMap();
//        return getMDCAwareCompletionStage(future)
//            .handle((value, throwable) -> {
//                TrackSupport.setMDCContext(contextMap);
//                if (throwable != null) {
//                    return throwableFunction.apply(throwable);
//                }
//                return value;
//            });
//    }
//}
