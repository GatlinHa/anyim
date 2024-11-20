package com.hibob.anyim.netty.mq.kafka;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hibob.anyim.common.protobuf.Msg;
import org.apache.kafka.common.serialization.Deserializer;

public class DeserializerMsg implements Deserializer<Msg> {
    @Override
    public Msg deserialize(String s, byte[] bytes) {
        try {
            return Msg.parseFrom(bytes);
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }
    }
}
