package com.hibob.anyim.netty.mq.kafka;

import com.hibob.anyim.netty.config.NacosConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ExtendWith(SpringExtension.class)
public class KafkaTest {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private NacosConfig nacosConfig;

    @Test
    public void test_01() throws InterruptedException {
        int count = 100;
        while (count > 0) {
            log.info("send message to kafka, count is {}", count);
            String instance = nacosConfig.getInstance();
            String topic = nacosConfig.getToTopic(instance);
            kafkaTemplate.send(topic, instance, "hello, kafka");
            TimeUnit.SECONDS.sleep(1);
            count--;
        }
    }
}
