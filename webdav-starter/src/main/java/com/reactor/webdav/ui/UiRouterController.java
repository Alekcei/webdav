package com.reactor.webdav.ui;

import com.reactor.webdav.WebDavIndexHtml;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;
import com.reactor.webdav.ui.icon.IconService;

import java.io.File;
import java.time.Duration;

// docs
// http://www.webdav.org/specs/rfc4918.html#rfc.section.9.4
// https://learn.microsoft.com/en-us/previous-versions/office/developer/exchange-server-2003/aa142960(v=exchg.65)
// http://citforum.ru/internet/webservers/webdav/
@Component
@Slf4j
public class UiRouterController implements WebDavIndexHtml {

    @Autowired
    private IconService iconService;

    String frontPath;

    @Value("${webdav.front.path:#{null}}")
    String frontFolder;
    @Bean
    @Order(-1)
    public RouterFunctions.Builder routeRequest2() {
        frontPath = "." +  File.separator + "front" + File.separator;
        if (frontFolder != null) {
            frontPath = frontFolder;
        }
        RouterFunctions.Builder builder = RouterFunctions.route();
        builder.route(new GetQueryParam("icon-file"),   this::iconFile); // заглушка для браузеров
        builder.route(new GetUi("ui"), this::getUi);               // Раздача статики
        return builder;

    }


    // заглушка для браузеров
    @SneakyThrows
    public Mono<ServerResponse> faviconIco(ServerRequest request) {
        return null;
    }

    @SneakyThrows
    public Mono<ServerResponse> getUi(ServerRequest serverRequest) {
        Resource resource = new FileSystemResource(frontPath + serverRequest.requestPath().subPath(2).value() );

        if (!resource.getFile().exists()  ||  serverRequest.headers().accept().contains(MediaType.parseMediaType("text/html"))) {
            return indexHtml(serverRequest);
        }


        return ServerResponse.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofHours(1)))
                .body(BodyInserters.fromResource(resource)  );
    }

    private Mono<ServerResponse> iconFile(ServerRequest serverRequest) {
        var queryParam = serverRequest.queryParams();
        Resource resource = iconService.getIconFromExtension(queryParam.getFirst("mimetype"));
        if (resource == null) {
            return  ServerResponse.notFound()
                    .cacheControl(CacheControl.maxAge(Duration.ofMinutes(30)))
                    .build();
        }

        return ServerResponse.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofMinutes(120)))
                .body(BodyInserters.fromResource(resource)  );
    }


    @Override
    public Mono<ServerResponse> indexHtml(ServerRequest serverRequest) {
        return ServerResponse.ok()
                .body(BodyInserters.fromResource(new FileSystemResource(frontPath + "index.html"))  );
    }
}

