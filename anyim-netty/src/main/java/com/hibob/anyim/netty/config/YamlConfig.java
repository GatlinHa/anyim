package com.hibob.anyim.netty.config;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Data
public class YamlConfig {

    @Value("${websocket.port}")
    private int port;

    @Value("${websocket.topic-distribute}")
    private String topicDistribute;

    public JSONObject getTopicDistribute() {
        return JSONObject.parseObject(topicDistribute);
    }

}
