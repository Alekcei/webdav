package ru.dump.response.dump_response;

import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.NettyWriteResponseFilter;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyResponseBodyGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

@Component
@Slf4j
public class ModifyResponseBodyFilter  implements GlobalFilter, Ordered {
    @Autowired
    private ModifyResponseBodyGatewayFilterFactory modifyResponseBodyGatewayFilterFactory;

    private static final String REQUEST_PREFIX = "\nRequest Info [ ";

    private static final String REQUEST_TAIL = " ]";

    private static final String RESPONSE_PREFIX = "Response Info [ ";

    private static final String RESPONSE_TAIL = " ]";

   //  private StringBuilder normalMsg = new StringBuilder();
    // https://www.baeldung.com/kotlin/spring-webflux-log-request-response-body
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        StringBuilder normalMsg = new StringBuilder();
        normalMsg.append("\n");
        normalMsg.append(exchange.getRequest().getMethodValue()).append(" ");
        normalMsg.append(exchange.getRequest().getPath());

        normalMsg.append("\n");
        exchange.getRequest().getHeaders().forEach((itKey, itVal) -> {
            normalMsg.append(itKey + ": " + itVal + "\n");
        });


        ServerHttpResponse response = exchange.getResponse();

        DataBufferFactory bufferFactory = response.bufferFactory();
        normalMsg.append("Response\n");
        ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(response) {
            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {

                normalMsg.append("status=").append(this.getStatusCode()).append("\n");
                this.getHeaders().forEach((itKey, itVal) -> {
                    normalMsg.append(itKey + ": " + itVal + "\n");
                });
                if (body instanceof Flux) {
                    Flux<? extends DataBuffer> fluxBody = (Flux<? extends DataBuffer>) body;
                    return super.writeWith(fluxBody.map(dataBuffer -> {
                        // probably should reuse buffers
                        byte[] content = new byte[dataBuffer.readableByteCount()];
                        dataBuffer.read(content);


                        String responseResult = new String(content, Charset.forName("UTF-8"));
                        normalMsg.append(";responseResult=\n").append(responseResult);
                        normalMsg.append("\n");
                        // log.info(normalMsg.toString());
                        return bufferFactory.wrap(content);
                    }));
                }

                return super.writeWith(body); // if body is not a flux. never got there.
            }
        };

        return chain.filter(exchange.mutate().request(request).response(decoratedResponse).build()).doFinally(it -> {
            log.info(normalMsg.toString());
        });
    }

    @Override
    public int getOrder() {
        return NettyWriteResponseFilter.WRITE_RESPONSE_FILTER_ORDER-2;
    }
}
