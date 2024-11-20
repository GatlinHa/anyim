package com.hibob.anyim.netty.config;

import com.hibob.anyim.common.protobuf.Msg;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.ProducerListener;

@Slf4j
@Configuration
@EnableConfigurationProperties({KafkaProperties.class})
@EnableKafka
@AllArgsConstructor
public class KafkaConfig {
    private final KafkaProperties kafkaProperties;

    @Bean
    public KafkaTemplate<String, Msg> kafkaTemplate(ProducerFactory<String, Msg> producerFactory,
                                                    ProducerListener<String, Msg> producerListener) {
        KafkaTemplate<String, Msg> kafkaTemplate = new KafkaTemplate<>(producerFactory());
        kafkaTemplate.setProducerListener(producerListener);
        return kafkaTemplate;
    }

    @Bean
    public ProducerFactory<String, Msg> producerFactory() {
        return new DefaultKafkaProducerFactory<>(kafkaProperties.buildProducerProperties());
    }

    /**
     * 生产者回调
     * @return
     */
    @Bean
    public ProducerListener<String, Msg> producerListener() {
        return new ProducerListener<String, Msg>() {
            @Override
            public void onSuccess(ProducerRecord<String, Msg> producerRecord, RecordMetadata recordMetadata) {
                log.info("Success to send ");
            }

            @Override
            public void onError(ProducerRecord<String, Msg> producerRecord, RecordMetadata recordMetadata, Exception exception) {
                log.error("Failed to send");
            }
        };
    }


}
