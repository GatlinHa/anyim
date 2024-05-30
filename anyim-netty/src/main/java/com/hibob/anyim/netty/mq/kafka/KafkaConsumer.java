package com.hibob.anyim.netty.mq.kafka;

import com.hibob.anyim.common.constants.RedisKey;
import com.hibob.anyim.common.utils.CommonUtil;
import com.hibob.anyim.netty.constants.Const;
import com.hibob.anyim.netty.protobuf.Msg;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import static com.hibob.anyim.netty.server.ws.WebSocketServer.getLocalRoute;

@Slf4j
@Component
public class KafkaConsumer {

    @KafkaListener(topics = "${websocket.consumer.topic}")
    public void onMessage(ConsumerRecord<String, Msg> record) {
        //TODO 调试信息，后面删掉
        log.info("kafka receive message, the partition is: {}, the key is: {}, the message is: \n{}", record.partition(), record.key(), record.value());

        Msg msg = record.value();
        if (msg.getHeader().getMagic() != Const.MAGIC) {
//            log.error("magic is not correct, the message is: \n{}", msg); //TODO 与调试信息重复，后面放开
            return;
        }

        String toId = msg.getBody().getToId();
        String toClient = msg.getBody().getToClient();
        String routeKey = RedisKey.NETTY_GLOBAL_ROUTE + CommonUtil.conUniqueId(toId, toClient);
        getLocalRoute().get(routeKey).writeAndFlush(msg);
    }

}
