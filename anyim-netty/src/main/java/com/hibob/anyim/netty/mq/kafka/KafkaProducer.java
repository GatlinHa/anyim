package com.hibob.anyim.netty.mq.kafka;

import com.hibob.anyim.netty.config.NacosConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KafkaProducer {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private NacosConfig nacosConfig;

    public void sendMessage(String instance, String message) {

        String toTopic = nacosConfig.getToTopic(instance);
        log.info("send message to kafka, topic is {}, message is {}", toTopic, message);
        // 设置分区key

        kafkaTemplate.send(toTopic, message);
    }

}
