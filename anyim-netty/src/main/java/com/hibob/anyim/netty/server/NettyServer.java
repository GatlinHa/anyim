package com.hibob.anyim.netty.server;

public interface NettyServer {

        boolean isReady();

        void start();

        void stop();
}
