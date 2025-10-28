package com.reactor.webdav.cors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsUtils;
import org.springframework.web.cors.reactive.DefaultCorsProcessor;
import org.springframework.web.server.ServerWebExchange;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.springframework.http.HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS;

//Переопределен так как есть кастомные методы
@Slf4j
class CustomDefaultCorsProcessor extends DefaultCorsProcessor {

    private static final List<String> VARY_HEADERS = Arrays.asList(
            HttpHeaders.ORIGIN, HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS);


    public boolean process(@Nullable CorsConfiguration config, ServerWebExchange exchange) {

        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        HttpHeaders responseHeaders = response.getHeaders();

        List<String> varyHeaders = responseHeaders.get(HttpHeaders.VARY);
        if (varyHeaders == null) {
            responseHeaders.addAll(HttpHeaders.VARY, VARY_HEADERS);
        } else {
            for (String header : VARY_HEADERS) {
                if (!varyHeaders.contains(header)) {
                    responseHeaders.add(HttpHeaders.VARY, header);
                }
            }
        }

        if (!CorsUtils.isCorsRequest(request)) {
            return true;
        }

        if (responseHeaders.getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN) != null) {
            log.trace("Skip: response already contains \"Access-Control-Allow-Origin\"");
            return true;
        }

        boolean preFlightRequest = CorsUtils.isPreFlightRequest(request);
        if (config == null) {
            if (preFlightRequest) {
                rejectRequest(response);
                return false;
            } else {
                return true;
            }
        }

        return handleInternal(exchange, config, preFlightRequest);
    }


    /**
     * Handle the given request.
     */
    protected boolean handleInternal(ServerWebExchange exchange,
                                     CorsConfiguration config, boolean preFlightRequest) {

        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        HttpHeaders responseHeaders = response.getHeaders();

        String requestOrigin = request.getHeaders().getOrigin();
        String allowOrigin = checkOrigin(config, requestOrigin);
        if (allowOrigin == null) {
            log.debug("Reject: '" + requestOrigin + "' origin is not allowed");
            rejectRequest(response);
            return false;
        }

        HttpMethod requestMethod = getMethodToUse(request, preFlightRequest);
        List<String> allowMethods = config.getAllowedMethods();//  List.of("POST", "PROPFIND", "GET", "OPTIONS");
        if (allowMethods == null) {
            log.debug("Reject: HTTP '" + requestMethod + "' is not allowed");
            rejectRequest(response);
            return false;
        }

        List<String> requestHeaders = getHeadersToUse(request, preFlightRequest);
        List<String> allowHeaders = checkHeaders(config, requestHeaders);
        if (preFlightRequest && allowHeaders == null) {
            log.debug("Reject: headers '" + requestHeaders + "' are not allowed");
            rejectRequest(response);
            return false;
        }

        responseHeaders.setAccessControlAllowOrigin(allowOrigin);

        if (preFlightRequest) {

            responseHeaders.set(ACCESS_CONTROL_ALLOW_METHODS, StringUtils.collectionToCommaDelimitedString(allowMethods));
        }

        if (preFlightRequest && !allowHeaders.isEmpty()) {
            responseHeaders.setAccessControlAllowHeaders(allowHeaders);
        }

        if (!CollectionUtils.isEmpty(config.getExposedHeaders())) {
            responseHeaders.setAccessControlExposeHeaders(config.getExposedHeaders());
        }

        if (Boolean.TRUE.equals(config.getAllowCredentials())) {
            responseHeaders.setAccessControlAllowCredentials(true);
        }

        if (preFlightRequest && config.getMaxAge() != null) {
            responseHeaders.setAccessControlMaxAge(config.getMaxAge());
        }

        return true;
    }

    /**
     * Check the origin and determine the origin for the response. The default
     * implementation simply delegates to
     * {@link CorsConfiguration#checkOrigin(String)}.
     */
    @Nullable
    protected String checkOrigin(CorsConfiguration config, @Nullable String requestOrigin) {
        return config.checkOrigin(requestOrigin);
    }


    @Nullable
    private HttpMethod getMethodToUse(ServerHttpRequest request, boolean isPreFlight) {
        return (isPreFlight ? request.getHeaders().getAccessControlRequestMethod() : request.getMethod());
    }

    /**
     * Check the headers and determine the headers for the response of a
     * pre-flight request. The default implementation simply delegates to
     * {@link CorsConfiguration#checkHeaders(List)}.
     */
    @Nullable

    protected List<String> checkHeaders(CorsConfiguration config, List<String> requestHeaders) {
        return config.checkHeaders(requestHeaders);
    }

    private List<String> getHeadersToUse(ServerHttpRequest request, boolean isPreFlight) {
        HttpHeaders headers = request.getHeaders();
        return (isPreFlight ? headers.getAccessControlRequestHeaders() : new ArrayList<>(headers.keySet()));
    }

}
