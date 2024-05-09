package com.infilos.spring.track.concurrent;

import com.infilos.spring.track.TrackSupport;

import java.util.concurrent.*;

@SuppressWarnings("NullableProblems")
public class MDCAwareThreadPoolExecutor extends ThreadPoolExecutor {
    public MDCAwareThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    public MDCAwareThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
    }

    public MDCAwareThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
    }

    public MDCAwareThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    }

    @Override
    public void execute(Runnable command) {
        super.execute(TrackSupport.wrapMDCContext(command));
    }

    @Override
    public Future<?> submit(Runnable task) {
        return super.submit(TrackSupport.wrapMDCContext(task));
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        return super.submit(TrackSupport.wrapMDCContext(task), result);
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return super.submit(TrackSupport.wrapMDCContext(task));
    }
}
