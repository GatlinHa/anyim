package com.hibob.anyim.netty.mq.kafka;

import com.hibob.anyim.netty.config.NacosConfig;
import com.hibob.anyim.netty.protobuf.Msg;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KafkaProducer {

    @Autowired
    private KafkaTemplate<String, Msg> kafkaTemplate;

    @Autowired
    private NacosConfig nacosConfig;

    public void sendMessage(String instance, Msg message) {
        String toTopic = nacosConfig.getToTopic(instance);
        kafkaTemplate.send(toTopic, nacosConfig.getInstance(), message);
        log.info("send message to kafka, topic is {}, message is {}", toTopic, message.toString());
    }

}
