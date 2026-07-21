package com.fanfaction.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean("aiTaskExecutor")
    public Executor aiTaskExecutor() {
        log.info("========== 初始化AI异步任务线程池 ==========");
        
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // 核心线程数
        executor.setCorePoolSize(5);
        log.info("核心线程数: {}", 5);
        
        // 最大线程数
        executor.setMaxPoolSize(10);
        log.info("最大线程数: {}", 10);
        
        // 队列容量
        executor.setQueueCapacity(100);
        log.info("队列容量: {}", 100);
        
        // 线程名称前缀
        executor.setThreadNamePrefix("ai-async-");
        log.info("线程名称前缀: ai-async-");
        
        // 线程空闲时间（秒）
        executor.setKeepAliveSeconds(60);
        log.info("线程空闲时间: {} 秒", 60);
        
        // 拒绝策略：由调用线程处理
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        log.info("拒绝策略: CallerRunsPolicy（由调用线程处理）");
        
        // 等待所有任务结束后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        
        // 等待时间（秒）
        executor.setAwaitTerminationSeconds(60);
        log.info("关闭等待时间: {} 秒", 60);
        
        executor.initialize();
        
        log.info("AI异步任务线程池初始化完成");
        log.info("线程池状态 - 活跃线程数: {}, 队列任务数: {}", 
                executor.getActiveCount(), executor.getThreadPoolExecutor().getQueue().size());
        log.info("========================================");
        
        return executor;
    }
}
