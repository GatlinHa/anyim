package com.hibob.anyim.common.config;

import com.alibaba.fastjson.JSONObject;
import com.hibob.anyim.common.utils.CommonUtil;
import lombok.Data;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import static com.hibob.anyim.common.constants.Const.SPLIT_C;

@Configuration
@Data
public class SnowflakeConfig implements InitializingBean {

    @Value("${server.port}")
    private int port;

    @Value("${custom.snow-flake.worker-datacenter-config:default}")
    private String workerDatacenterConfig;

    public String getInstance() {
        return CommonUtil.getLocalIp() + SPLIT_C + port;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (!workerDatacenterConfig.equals("default")) {
            JSONObject jsonObject = JSONObject.parseObject(workerDatacenterConfig);
            int workerId = (int) jsonObject.getJSONObject(getInstance()).get("worker-id");
            int datacenterId = (int) jsonObject.getJSONObject(getInstance()).get("datacenter-id");
            System.setProperty("custom.snow-flake.worker-id", String.valueOf(workerId));
            System.setProperty("custom.snow-flake.datacenter-id", String.valueOf(datacenterId));
        }
    }

}
