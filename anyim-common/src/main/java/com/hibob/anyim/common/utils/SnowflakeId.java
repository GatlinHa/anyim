package com.hibob.anyim.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SnowflakeId {

    private final long startTimestamp = 1704038400000L; // 开始时间戳，2024-01-01 00:00:00

    private final long workerIdBits = 5L; // 工作机器ID的位数
    private final long datacenterIdBits = 5L; // 数据中心ID的位数
    private final long maximumWorkerId = ~(-1L << workerIdBits); // 最大工作机器ID
    private final long maximumDatacenterId = ~(-1L << datacenterIdBits); // 最大数据中心ID
    private final long sequenceBits = 12L; // 序列号的位数

    private final long workerIdShift = sequenceBits; // 工作机器ID的偏移量
    private final long datacenterIdShift = sequenceBits + workerIdBits; // 数据中心ID的偏移量
    private final long timestampLeftShift = sequenceBits + workerIdBits + datacenterIdBits; // 时间戳的偏移量

    private long workerId; // 工作机器ID

    private long datacenterId; // 数据中心ID
    private long lastTimestamp = -1L; // 上次生成ID的时间戳
    private long sequence = 0L; // 序列号

    public SnowflakeId() {
        long workerId = Long.parseLong(System.getProperties().getProperty("custom.snow-flake.worker-id"));
        long datacenterId = Long.parseLong(System.getProperties().getProperty("custom.snow-flake.datacenter-id"));
        // 初始化工作机器ID和数据中心ID
        if (workerId > maximumWorkerId || workerId < 0) {
            throw new IllegalArgumentException("Worker ID can't be greater than " + maximumWorkerId + " or less than 0");
        }
        if (datacenterId > maximumDatacenterId || datacenterId < 0) {
            throw new IllegalArgumentException("Datacenter ID can't be greater than " + maximumDatacenterId + " or less than 0");
        }
        this.workerId = workerId;
        this.datacenterId = datacenterId;
    }

    public synchronized long nextId() {
        // 生成ID
        long currentTimestamp = System.currentTimeMillis();
        if (currentTimestamp < lastTimestamp) {
            throw new RuntimeException("Clock moved backwards. Refusing to generate ID for " + (lastTimestamp - currentTimestamp) + " milliseconds");
        }

        if (currentTimestamp == lastTimestamp) {
            sequence = (sequence + 1) & ((1 << sequenceBits) - 1);

            if (sequence == 0) {
                // 序列号已经达到最大值，等到下一毫秒再生成
                currentTimestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }

        lastTimestamp = currentTimestamp;

        // 组合ID
        return ((currentTimestamp - startTimestamp) << timestampLeftShift)
                | (datacenterId << datacenterIdShift)
                | (workerId << workerIdShift)
                | sequence;
    }

    private long tilNextMillis(long lastTimestamp) {
        // 获取下一毫秒时间戳
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }
}