package com.hibob.anyim.agw.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.reactive.socket.client.TomcatWebSocketClient;
import org.springframework.web.reactive.socket.client.WebSocketClient;
import org.springframework.web.reactive.socket.server.RequestUpgradeStrategy;
import org.springframework.web.reactive.socket.server.upgrade.TomcatRequestUpgradeStrategy;

/**
 *  Gateway默认使用基于Netty容器的RequestUpgradeStrategy和WebSocketClient导致报错，声明Tomcat容器对应的Bean来覆盖它可以解决这个问题:
 *  gateway 转发weboskct 报错 org.apache.catalina.connector.ResponseFacade cannot be cast to reactor.netty.http.server.HttpServerResponse
 */
@Configuration
public class WebSocketConf {

    @Bean
    @Primary
    public RequestUpgradeStrategy requestUpgradeStrategy() {
        return new TomcatRequestUpgradeStrategy();
    }

    @Bean
    @Primary
    public WebSocketClient webSocketClient() {
        return new TomcatWebSocketClient();
    }
}
