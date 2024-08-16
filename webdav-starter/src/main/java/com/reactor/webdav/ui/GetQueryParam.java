package com.reactor.webdav.ui;

import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.server.RequestPredicate;
import org.springframework.web.reactive.function.server.ServerRequest;

public class GetQueryParam implements RequestPredicate {
    final String param;
    public GetQueryParam(String param) {
        this.param = param.toLowerCase();
    }

    @Override
    public boolean test(ServerRequest request) {
        return testCalc(request, param);
    }

    public static boolean testCalc(ServerRequest request, String param) {
        if (request.method()== HttpMethod.GET && request.exchange().getRequest().getQueryParams().containsKey(param)) return true;
        return false;
    }
}