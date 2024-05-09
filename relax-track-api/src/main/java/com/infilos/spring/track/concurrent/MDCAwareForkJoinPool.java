//package com.infilos.spring.track.concurrent;
//
//import com.infilos.spring.track.TrackSupport;
//
//import java.util.concurrent.*;
//
//public class MDCAwareForkJoinPool extends ForkJoinPool {
//
//    public MDCAwareForkJoinPool() {
//        super();
//    }
//
//    /**
//     * @param parallelism – the parallelism level
//     */
//    public MDCAwareForkJoinPool(int parallelism) {
//        super(parallelism);
//    }
//
//    /**
//     * @param parallelism the parallelism level. For default value,
//     *                    use {@link java.lang.Runtime#availableProcessors}.
//     * @param factory     the factory for creating new threads. For default value,
//     *                    use {@link #defaultForkJoinWorkerThreadFactory}.
//     * @param handler     the handler for internal worker threads that
//     *                    terminate due to unrecoverable errors encountered while executing
//     *                    tasks. For default value, use {@code null}.
//     * @param asyncMode   if true,
//     *                    establishes local first-in-first-out scheduling mode for forked
//     *                    tasks that are never joined. This mode may be more appropriate
//     *                    than default locally stack-based mode in applications in which
//     *                    worker threads only process event-style asynchronous tasks.
//     *                    For default value, use {@code false}.
//     */
//    public MDCAwareForkJoinPool(int parallelism,
//                                ForkJoinWorkerThreadFactory factory,
//                                Thread.UncaughtExceptionHandler handler,
//                                boolean asyncMode) {
//        super(parallelism, factory, handler, asyncMode);
//    }
//
//    @Override
//    public <T> ForkJoinTask<T> submit(Callable<T> task) {
//        return super.submit(TrackSupport.wrapMDCContext(task));
//    }
//
//    @Override
//    public <T> ForkJoinTask<T> submit(Runnable task, T result) {
//        return super.submit(TrackSupport.wrapMDCContext(task), result);
//    }
//
//    @Override
//    public ForkJoinTask<?> submit(Runnable task) {
//        return super.submit(TrackSupport.wrapMDCContext(task));
//    }
//
//    @Override
//    public void execute(Runnable task) {
//        super.execute(TrackSupport.wrapMDCContext(task));
//    }
//}
