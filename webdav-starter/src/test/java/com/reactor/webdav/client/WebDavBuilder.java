package com.reactor.webdav.client;

import io.netty.handler.codec.http.HttpMethod;
import lombok.Data;
import lombok.SneakyThrows;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.util.Base64Utils;
import reactor.core.publisher.Mono;
import reactor.netty.ByteBufFlux;
import reactor.netty.ByteBufMono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.client.HttpClientResponse;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

@Data
public class WebDavBuilder {
    String method;
    String url;

    String path;
    String user;
    String password;
    String destination;
    String depth;
    Map<String, String> headers = new HashMap<>();

    public WebDavBuilder setMethod(String method){
        this.method = method;
        return this;
    }

    public WebDavBuilder newMethod(String method){
        this.method = method;
        return this;
    }

    public WebDavBuilder setUrl(String url){
        this.url = url;
        return this;
    }

    public WebDavBuilder setPath(String path){
        this.path = path;
        return this;
    }

    public WebDavBuilder addHeader(String path, String s){
        headers.put(path, s);
        return this;
    }
    public WebDavBuilder setDepth(String depth) {
        this.depth = depth;
        return this;
    }

    public WebDavBuilder setDestination(String destination) {
        this.destination = destination;
        return this;
    }

    public WebDavBuilder addUser(String user){
        this.user = user;
        return this;
    }

    public WebDavBuilder addPassword(String password){
        this.password = password;
        return this;
    }

    Object fileLink;

    public WebDavBuilder addFile(File file) {
        fileLink = file;
        return this;
    }

    @SneakyThrows
    private HttpClient.ResponseReceiver<?> getRequestSender() {


        HttpClient client = HttpClient.create();
        var rq = client
            .headers(it -> {
                if (headers != null && !headers.isEmpty()) {
                    headers.forEach(it::add);
                }

                if (user != null) {
                    it.add(
                        "Authorization",
                        "Basic " + Base64Utils.encodeToString(String.join(":", user, password!=null?password:"").getBytes())
                    );
                }

                if (destination != null) {
                    it.add(
                        "Destination",
                        destination
                    );
                }

                if (depth != null) {
                    it.add(
                        "Depth",
                        depth
                    );
                }

            })
            .request(new HttpMethod(method.toUpperCase()))
            .uri(url + path);
        Resource resource = null;
        if (fileLink instanceof String) {
            resource = new FileSystemResource((String)fileLink);
        } else if(fileLink instanceof File) {
            resource = new FileSystemResource((File)fileLink);
        }

        if (resource != null) {
            return rq
                    .send(ByteBufFlux.fromPath(resource.getFile().toPath()));
        }

        return rq;
    }


    @SuppressWarnings("ConstantConditions")
    public Mono<HttpClientResponse> response(){
        return getRequestSender().response();
    }

    public <V> Mono<?> responseSingle(BiFunction<? super HttpClientResponse, ? super ByteBufMono, ? extends Mono<V>> receiver){
        return getRequestSender().responseSingle(receiver);
    }

    public HttpClientResponse block(){
        return response().block();
    }

}
