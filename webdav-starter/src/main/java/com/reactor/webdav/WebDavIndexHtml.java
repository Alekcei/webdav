package com.reactor.webdav;

import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

public interface WebDavIndexHtml {
    public Mono<ServerResponse> indexHtml(ServerRequest serverRequest);
}
