package com.hibob.anyim.netty.mq.kafka;

import com.hibob.anyim.netty.config.NacosConfig;
import com.hibob.anyim.common.protobuf.Msg;
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

    public void sendChatMessage(String instance, Msg message) {
        String toTopic = nacosConfig.getToTopic(instance);
        kafkaTemplate.send(toTopic, message.getBody().getFromId(), message);
        log.info("send message to kafka, topic is {}, message is {}", toTopic, message);
    }

    public void sendGroupChatMessage(String instance, Msg message) {
        String toTopic = nacosConfig.getToTopic(instance);
        kafkaTemplate.send(toTopic, message.getBody().getGroupId(), message);
        log.info("send message to kafka, topic is {}, message is {}", toTopic, message);
    }

}
