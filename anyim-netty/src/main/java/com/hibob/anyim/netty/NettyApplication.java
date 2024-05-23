package com.hibob.anyim.netty;

import com.hibob.anyim.netty.server.NettyServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@Slf4j
@EnableDiscoveryClient
@SpringBootApplication//(exclude = {SecurityAutoConfiguration.class}) // 禁用secrity，不需要Security的登录，会自己实现，而且不禁用swagger打开要密码
public class NettyApplication implements CommandLineRunner {

    @Autowired
    NettyServer nettyServer;

    public static void main(String[] args) {
        SpringApplication.run(NettyApplication.class, args);

    }

    @Override
    public void run(String... args) throws Exception {
        //启动netty
        log.info("启动netty服务===================================");
        // TODO 获取Nacos中注册的实例id
        nettyServer.start();
    }
}

