package com.reactor.webdav;

import org.springframework.web.reactive.function.server.RequestPredicate;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.util.List;

public class CustomHeader implements RequestPredicate {
    final String method;
    CustomHeader(String method) {
        this.method = method.toLowerCase();
    }

    @Override
    public boolean test(ServerRequest request) {
        return testCalc(request, method);
    }

    public static boolean testCalc(ServerRequest request, String method) {
        if (method.equals("other")) return true;
        if (request.exchange().getRequest().getMethodValue().toLowerCase().equals(method)) return true;
        if (request.exchange().getRequest().getHeaders().getOrDefault("CustomMethod", List.of()).contains(method)) return true;
        return false;
    }
}