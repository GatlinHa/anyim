package com.hibob.anyim.netty.server.ws;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.hibob.anyim.common.utils.CommonUtil;
import com.hibob.anyim.netty.protobuf.Msg;
import com.hibob.anyim.netty.server.handler.*;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.Resource;
import java.net.InetAddress;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketServer {
    @Value("${websocket.name}")
    private String name;

    @Value("${websocket.port}")
    private int port;

    @Value("${websocket.path}")
    private String path;

    @Value("${websocket.log-level}")
    private String logLevel;

    @Resource
    private NacosDiscoveryProperties nacosDiscoveryProperties;

    private static Map<String, Channel> localRoute = new ConcurrentHashMap<>(); // TODO 要考虑channel的老化，用map可能不太合适

    private volatile boolean ready = false;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workGroup;


    public static Map<String, Channel> getLocalRoute() {
        return localRoute;
    }

    public void start() {
        ServerBootstrap bootstrap = new ServerBootstrap();
        bossGroup = new NioEventLoopGroup();
        workGroup = new NioEventLoopGroup();

        try {
            bootstrap.group(bossGroup, workGroup) // 设置为主从线程模型
                    .channel(NioServerSocketChannel.class) // 设置服务端NIO通信类型
                    .option(ChannelOption.SO_BACKLOG, 128) //设置主线程池线程队列个数
                    .childOption(ChannelOption.SO_KEEPALIVE, true) // 设置保持活动连接状态
                    .childHandler(new ChannelInitializer<Channel>() { // 设置ChannelPipeline，也就是业务职责链，由处理的Handler串联而成，由从线程池处理
                        @Override
                        protected void initChannel(Channel channel) throws Exception {
                            ChannelPipeline pipeline = channel.pipeline();
                            pipeline.addLast(new LoggingHandler(LogLevel.valueOf(logLevel.toUpperCase()))); // 增加日志打印的handler，这里要配合logback的日志级别配置文件，把LoggingHandler的全限定名的日志级别也改为debug
                            pipeline.addLast(new IdleStateHandler(300, 0, 0, TimeUnit.SECONDS)); // 用来判断 是不是读 空闲时间过长，或写空闲时间过长 (读，写，读写空闲时间限制) 0表示不关心
                            pipeline.addLast(new HttpServerCodec()); //HTTP协议编解码器，用于处理HTTP请求和响应的编码和解码。其主要作用是将HTTP请求和响应消息转换为Netty的ByteBuf对象，并将其传递到下一个处理器进行处理。
                            pipeline.addLast(new HttpObjectAggregator(65535)); //加了这行会导致postman出现1006错误 用于HTTP服务端，将来自客户端的HTTP请求和响应消息聚合成一个完整的消息，以便后续的处理。
                            pipeline.addLast(new ChunkedWriteHandler()); // 支持分块写入  在网络通信中，如果要传输的数据量较大，直接将数据一次性写入到网络缓冲区可能会导致内存占用过大或者网络拥塞等问题
                            pipeline.addLast(new AuthorizationHandler()); //处理登录
                            pipeline.addLast(new WebSocketServerCompressionHandler()); // WebSocket数据压缩
                            pipeline.addLast(new WebSocketServerProtocolHandler(path, true)); // WebSocket协议处理器，如果以url拼接方式传参，第二个参数要设置成true
                            pipeline.addLast(new WebSocketToByteBufEncoder()); //解码：WebSocketFrame -> ByteBuf
                            pipeline.addLast(new ProtobufVarint32FrameDecoder()); //解码：处理半包黏包，参数类型是ByteBuf
                            pipeline.addLast(new ProtobufDecoder(Msg.getDefaultInstance())); //解码：ByteBuf -> Msg
                            pipeline.addLast(new ByteBufToWebSocketFrame()); //编码：ByteBuf -> WebSocketFrame(二进制)
                            pipeline.addLast(new ProtobufVarint32LengthFieldPrepender()); //编码：处理半包黏包，参数类型是ByteBuf
                            pipeline.addLast(new ProtobufEncoder()); //编码：Msg -> ByteBuf
                            pipeline.addLast(new MsgServerHandler()); // 业务处理理器，读写都是Msg
                        }
                    });

            ChannelFuture future = bootstrap.bind(port).sync(); //绑定端口号，启动服务端，用同步sync()是因为上一步是异步非阻塞操作，如果想要拿到结果需要额外sync()
            registerWsToNacos();
            future.channel().closeFuture().sync(); //对通道关闭进行监听
        }
        catch (Exception e) {
            log.error("WebSocket Server start failed on port: {}", port);
        }
        finally {
            log.info("shutdown gracefully");
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }

    }

    private void registerWsToNacos() {
        try {
            Properties properties = new Properties();
            properties.setProperty(PropertyKeyConst.NAMESPACE, nacosDiscoveryProperties.getNamespace());
            properties.setProperty(PropertyKeyConst.SERVER_ADDR, nacosDiscoveryProperties.getServerAddr());
            NamingService namingService = NamingFactory.createNamingService(properties);
            namingService.registerInstance(name, CommonUtil.getLocalIp(), port);
        } catch (Exception e) {
            log.error("register websocket server to nacos error: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

}
