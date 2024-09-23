package com.reactor.webdav.configure;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.stereotype.Component;

@Component
public class IdleTimeCustomizer implements WebServerFactoryCustomizer<NettyReactiveWebServerFactory> {

    @Value("${webdav.idleTimeSeconds:#{null}}")
    Integer allIdleTimeSeconds = 61; // сек
    @Autowired
    private HttpHandler httpHandler;

    @SuppressWarnings("deprecation")
    @Override
    public void customize(NettyReactiveWebServerFactory factory) {
        // https://projectreactor.io/docs/netty/release/reference/index.html#_http_access_log
        factory.addServerCustomizers(server ->
                // свой регистратор метрик

                server.doOnConnection(connection ->
                        connection.addHandler(
                                new IdleStateHandler(
                                        0, 0, allIdleTimeSeconds
                                ) {
                                    @Override
                                    protected void channelIdle(
                                            ChannelHandlerContext ctx,
                                            IdleStateEvent evt
                                    ) {

//                                        ctx.fireExceptionCaught(
//                                                evt.state() == WRITER_IDLE_STATE_EVENT.state()
//                                                        ? WriteTimeoutException.INSTANCE
//                                                        : ReadTimeoutException.INSTANCE
//                                        );
                                        ctx.write(new CloseWebSocketFrame());
                                        ctx.close();
                                    }
                                }
                        )
                )


        );



        // https://www.baeldung.com/spring-boot-reactor-netty
        // https://piotrminkowski.com/2021/05/24/ssl-with-spring-webflux-and-vault-pki/
//        Ssl ssl = new Ssl();
//        ssl.setEnabled(true);
//        ssl.setKeyStore("classpath:sample.jks");
//        ssl.setKeyAlias("alias");
//        ssl.setKeyPassword("password");
//        ssl.setKeyStorePassword("secret");
//
//        serverFactory.setSsl(ssl);
//        Http2 http2 = new Http2();
//        http2.setEnabled(false);
//        serverFactory.addServerCustomizers(new SslServerCustomizer(ssl, http2, null));
//        serverFactory.setPort(8443);
//
//        serverFactory.setPort(8088);
    }
}
