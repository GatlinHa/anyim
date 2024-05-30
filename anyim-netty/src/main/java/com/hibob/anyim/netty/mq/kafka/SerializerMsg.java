package com.hibob.anyim.netty.mq.kafka;

import com.hibob.anyim.netty.protobuf.Msg;
import org.apache.kafka.common.serialization.Serializer;

public class SerializerMsg implements Serializer<Msg> {
    @Override
    public byte[] serialize(String s, Msg msg) {
        return msg.toByteArray();
    }
}
