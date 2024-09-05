package com.reactor.webdav.configure;

import io.netty.channel.unix.Errors;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

@Component
public class CustomHttpHandlerDecoratorFactory implements WebExceptionHandler {

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        // при 206 пропускаем вывод всех ошибок, так как клиент закрывает соединение
        if (ex instanceof Errors.NativeIoException && exchange.getResponse().getStatusCode().is2xxSuccessful()) {
            return Mono.empty();
        }
        return Mono.error(ex);
    }
}
