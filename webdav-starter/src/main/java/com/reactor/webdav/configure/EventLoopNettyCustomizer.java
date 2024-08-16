package com.reactor.webdav.configure;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.embedded.netty.NettyServerCustomizer;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import reactor.netty.http.server.HttpServer;

// @Component
class EventLoopNettyCustomizer implements NettyServerCustomizer {

    @Autowired
    private HttpHandler httpHandler;

    @Override
    public HttpServer apply(HttpServer httpServer) {
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup(100);

        eventLoopGroup.register(new NioServerSocketChannel());
        return httpServer.runOn(eventLoopGroup);
    }
}