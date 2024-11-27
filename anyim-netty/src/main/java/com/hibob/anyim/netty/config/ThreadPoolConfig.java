package com.hibob.anyim.netty.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class ThreadPoolConfig {

    /**
     * 机器的CPU核数:Runtime.getRuntime().availableProcessors()
     * corePoolSize 池中所保存的线程数，包括空闲线程。
     * CPU 密集型：核心线程数 = CPU核数 + 1
     * IO 密集型：核心线程数 = CPU核数 * 2
     */
    private int corePoolSize = Runtime.getRuntime().availableProcessors() * 2;
    /**
     * maximumPoolSize - 池中允许的最大线程数(采用LinkedBlockingQueue时没有作用)。
     */
    @Value("${custom.thread-pool.max-pool-size:100}")
    private int maxPoolSize;
    /**
     * keepAliveTime -当线程数大于核心时，此为终止前多余的空闲线程等待新任务的最长时间，线程池维护线程所允许的空闲时间
     */
    @Value("${custom.thread-pool.keep-alive-time:1000}")
    private int KeepAliveTime;
    /**
     * 等待队列的大小。默认是无界的，性能损耗的关键
     */
    @Value("${custom.thread-pool.queue-size:200}")
    private int queueSize;

    @Bean
    public ThreadPoolExecutor threadPoolExecutor() {
        return new ThreadPoolExecutor(
                corePoolSize,
                maxPoolSize,
                KeepAliveTime,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(queueSize),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy() //当任务无法被执行时，直接抛出 RejectedExecutionException 异常
        );
    }


}
