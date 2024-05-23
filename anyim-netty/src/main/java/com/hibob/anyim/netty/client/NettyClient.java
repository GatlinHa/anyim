package com.hibob.anyim.netty.client;

import com.hibob.anyim.netty.client.handler.ClientHandler;
import com.hibob.anyim.netty.constants.Const;
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
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Scanner;

@Slf4j
public class NettyClient {
    private static final String path = "/ws";

    public static void main(String[] args) throws URISyntaxException {
        NioEventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        URI uri = new URI("ws://localhost:8100/ws");
        DefaultHttpHeaders headers = new DefaultHttpHeaders();
        headers.add(HttpHeaderNames.AUTHORIZATION, "123");
        headers.add("account", "account_001");
        headers.add("clientId", "client_001");
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
                                            uri, // TODO ?accessToken=123
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

            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            Header header = Header.newBuilder()
                                    .setMagic(Const.MAGIC)
                                    .setVersion(0)
                                    .setMsgType(MsgType.HEART_BEAT)
                                    .build();
                            Msg msgOut = Msg.newBuilder().setHeader(header).build();
                            channelFuture.channel().writeAndFlush(msgOut);
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }).start();

            Scanner scanner = new Scanner(System.in);
            while (true) {
                String line = scanner.nextLine();
                if ("exit".equals(line)) {
                    break;
                }
                Header header = Header.newBuilder()
                        .setMagic(Const.MAGIC)
                        .setVersion(0)
                        .setMsgType(MsgType.CHAT)
                        .setIsExtension(false)
                        .build();
                ChatBody body = ChatBody.newBuilder()
                        .setFromId("1")
                        .setFromDev("1")
                        .setToId("2")
                        .setToDev("2")
                        .setSeq(1)
                        .setContent(line)
                        .build();
                Msg msg = Msg.newBuilder().setHeader(header).setChatBody(body).build();
                channelFuture.channel().writeAndFlush(msg).addListener((ChannelFutureListener) future -> {
                            if (future.isSuccess()) {
                                log.info("send success");
                            }
                            else {
                                log.error("send failed, future.cause(): {}", future.cause());
                            }
                });
            }
            channelFuture.channel().closeFuture().sync();

        }
        catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }
        finally {
            group.shutdownGracefully();
        }
    }
}
