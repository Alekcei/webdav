package com.reactor.webdav.cors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.support.RouterFunctionMapping;

import javax.annotation.PostConstruct;

@Configuration
@ConditionalOnProperty(prefix = "webdav.cors", name = "origins")
class MappingConfig {
    @Autowired
    RouterFunctionMapping mapping;

    @PostConstruct
    void init() {
        mapping.setCorsProcessor(new CustomDefaultCorsProcessor());
    }
}
