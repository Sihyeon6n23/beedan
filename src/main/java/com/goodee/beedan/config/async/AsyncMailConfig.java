package com.goodee.beedan.config.async;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync // 비동기 기능을 활성화합니다.
public class AsyncMailConfig implements AsyncConfigurer {

    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 핵심 스레드 수 (항상 유지되는 스레드)
        executor.setCorePoolSize(5);

        // 최대 스레드 수 (작업이 많아질 때 확장되는 최대치)
        executor.setMaxPoolSize(10);

        // 큐 용량 (작업이 몰릴 때 대기하는 공간)
        executor.setQueueCapacity(100);

        // 스레드 이름 접두사 (로그 확인용)
        executor.setThreadNamePrefix("BEEDAN-MAIL-");

        executor.initialize();
        return executor;
    }
}
