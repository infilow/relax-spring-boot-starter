package com.infilos.spring.config;

import com.infilos.spring.track.TrackSupport;
import org.springframework.boot.task.TaskExecutorBuilder;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurerSupport;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@EnableAsync
@Configuration
public class AsyncExecutorConfigure extends AsyncConfigurerSupport {

    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new TaskExecutorBuilder()
            // omitted add other configs here...
            .taskDecorator(TrackSupport.getMDCContextTaskDecorator())
            .build();
        executor.initialize();
        
        return executor;
    }
}
