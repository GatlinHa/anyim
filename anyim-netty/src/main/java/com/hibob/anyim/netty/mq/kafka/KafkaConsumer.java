package com.hibob.anyim.netty.mq.kafka;

import com.hibob.anyim.common.constants.Const;
import com.hibob.anyim.common.constants.RedisKey;
import com.hibob.anyim.common.utils.CommonUtil;
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
        Msg msg = record.value();
        if (msg.getHeader().getMagic() != Const.MAGIC) {
            log.error("magic is not correct, the message is: \n{}", msg);
            return;
        }

        String toId = msg.getBody().getToId();
        String toClient = msg.getBody().getToClient();
        String routeKey = RedisKey.NETTY_GLOBAL_ROUTE + CommonUtil.conUniqueId(toId, toClient);
        getLocalRoute().get(routeKey).writeAndFlush(msg);
    }

}
