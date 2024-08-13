package com.reactor.webdav.ui;

import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.server.RequestPredicate;
import org.springframework.web.reactive.function.server.ServerRequest;

public class GetUi implements RequestPredicate {
    final String prefix;
    public GetUi(String param) {
        this.prefix = param.toLowerCase();
    }

    @Override
    public boolean test(ServerRequest request) {
        return testCalc(request, prefix);
    }

    public static boolean testCalc(ServerRequest request, String param) {
        if (request.method() == HttpMethod.GET &&
            request.exchange().getRequest().getPath().elements().size() > 1 &&
            request.exchange().getRequest().getPath().subPath(1,2).value().equals(param)) return true;

        return false;
    }
}