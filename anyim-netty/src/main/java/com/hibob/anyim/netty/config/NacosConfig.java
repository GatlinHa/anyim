package com.hibob.anyim.netty.config;

import com.alibaba.cloud.nacos.NacosServiceManager;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Data
public class NacosConfig {

    private final NacosServiceManager nacosServiceManager;

    @Value("${spring.application.name}")
    private String serverName;

    @Value("${spring.profiles.active}")
    private String springProfilesActive;

    @Value("${spring.cloud.nacos.discovery.server-addr}")
    private String discoveryServerAddr;

    // 这个port只能是微服务的port，不能是netty的port，因为要去注册中心校验netty实例是否是有效实例
    @Value("${server.port}")
    private int port;

    @Value("${websocket.topic-distribute}")
    private String topicDistribute;

    public NacosConfig(NacosServiceManager nacosServiceManager) {
        this.nacosServiceManager = nacosServiceManager;
    }

    public JSONObject getTopicDistribute() {
        return JSONObject.parseObject(topicDistribute);
    }

    public List<Instance> getNettyInstances() throws NacosException {
        NamingService namingService = nacosServiceManager.getNamingService();
        List<Instance> instances = namingService.selectInstances(serverName, true);
        return instances;
    }

    public boolean isValidInstance(String instance) throws NacosException {
        List<Instance> nettyInstances = getNettyInstances();
        for (Instance ins : nettyInstances) {
            String s = ins.getIp() + ":" + ins.getPort();
            if (s.equals(instance)) {
                return true;
            }
        }
        return false;
    }

}
