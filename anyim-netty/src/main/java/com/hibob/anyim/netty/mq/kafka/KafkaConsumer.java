package com.hibob.anyim.netty.mq.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KafkaConsumer {

    @KafkaListener(topics = "${websocket.consumer.topic}")
    public void onMessage(ConsumerRecord<String, String> record) {
        log.info("kafka receive message, the partition is: {}, the key is: {}, the message is: {}", record.partition(), record.key(), record.value());
    }

}
