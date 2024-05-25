package com.hibob.anyim.netty.client;

import com.alibaba.fastjson.JSONObject;
import com.hibob.anyim.netty.handler.ClientHandler;
import com.hibob.anyim.netty.protobuf.*;
import com.hibob.anyim.netty.server.handler.ByteBufToWebSocketFrame;
import com.hibob.anyim.netty.server.handler.WebSocketToByteBufEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.assertTrue;

/**
 * 启动测试前要先启动User服务
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class NettyClientTest {

    private String token ="";

    private final UserClient userClient = new UserClient(
            "netty_test_account_01",
            "netty_test_clientId_01",
            "netty_test_headImage_01",
            "netty_test_inviteCode_01",
            "netty_test_nickName_01",
            "123456",
            "netty_test_phoneNum_01"
    );

    @Before
    public void prepareUser() throws URISyntaxException {
        userClient.register();
        JSONObject loginRet = userClient.login();
        token = (String) ((JSONObject)loginRet.get("data")).get("accessToken");
    }

    private void clearUser() throws URISyntaxException {
        userClient.deregister(token);
    }

    @Test
    public void Test01() throws URISyntaxException {
        Boolean ret1 = false;
        NioEventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        URI uri = new URI("ws://localhost:8100/ws");
        DefaultHttpHeaders headers = new DefaultHttpHeaders();
        headers.add(HttpHeaderNames.AUTHORIZATION, token);
        headers.add("account", userClient.getAccount());
        headers.add("clientId", userClient.getClientId());
        try {
            bootstrap
                    .group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new LoggingHandler(LogLevel.DEBUG));
                            pipeline.addLast(new HttpClientCodec());
                            pipeline.addLast(new ChunkedWriteHandler());
                            pipeline.addLast(new HttpObjectAggregator(65536));
                            pipeline.addLast(new WebSocketServerCompressionHandler()); // WebSocket数据压缩
                            pipeline.addLast(new WebSocketClientProtocolHandler(
                                    WebSocketClientHandshakerFactory.newHandshaker(
                                            uri,
                                            WebSocketVersion.V13,
                                            (String)null,
                                            false,
                                            headers)));
                            pipeline.addLast(new WebSocketToByteBufEncoder()); //解码：WebSocketFrame -> ByteBuf
                            pipeline.addLast(new ProtobufVarint32FrameDecoder()); //解码：处理半包黏包，参数类型是ByteBuf
                            pipeline.addLast(new ProtobufDecoder(Msg.getDefaultInstance())); //解码：ByteBuf -> Msg
                            pipeline.addLast(new ByteBufToWebSocketFrame()); //编码：ByteBuf -> WebSocketFrame(二进制)
                            pipeline.addLast(new ProtobufVarint32LengthFieldPrepender()); //编码：处理半包黏包，参数类型是ByteBuf
                            pipeline.addLast(new ProtobufEncoder()); //编码：Msg -> ByteBuf
                            pipeline.addLast(new ClientHandler()); // 业务处理理器，读写都是Msg
                        }
                    });
            ChannelFuture channelFuture = bootstrap.connect(uri.getHost(), uri.getPort()).sync();
            channelFuture.channel().closeFuture().sync();
            Msg msg = (Msg) channelFuture.channel().attr(AttributeKey.valueOf("msg")).get();
            ret1 = msg.getHeader().getMsgType() == MsgType.HELLO;
        }
        catch (Exception e) {
            log.error(e.getMessage());
        }
        finally {
            clearUser();
            assertTrue(ret1);
        }

    }
}
