package com.reactor.webdav.configure;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.util.Iterator;
import java.util.List;

@Configuration
public class RouterBuilderConfigure {

    @Bean
    public RouterFunction<ServerResponse> buildRoute(
            @Autowired List<RouterFunctions.Builder> builders
    ) {

        if (builders.size() == 1) {
            return builders.get(0).build();
        }
        Iterator<RouterFunctions.Builder> buildIterator = builders.iterator();
        RouterFunction<ServerResponse> b = buildIterator.next().build();
        while (buildIterator.hasNext()) {

            b = b.and(buildIterator.next().build());
        }
        return b;
    }
}
