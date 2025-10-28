package com.reactor.webdav.cors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;

import org.springframework.web.reactive.config.EnableWebFlux;

import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.springframework.http.HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS;

@Configuration
@EnableWebFlux
@ConditionalOnProperty(prefix = "webdav.cors", name = "origins")
public class CorsConfig implements WebFluxConfigurer {

    @Value("${webdav.cors.origins:#{null}}")
    String[] origins;


    @Autowired
    ApplicationContext ctx;

    @Override
    public void addCorsMappings(CorsRegistry corsRegistry) {

        corsRegistry.addMapping("/**")
                .allowedOrigins(origins)
                .allowedMethods("GET", "POST", "PROPFIND", "OPTIONS"); // put the http verbs you want allow

    }

}

