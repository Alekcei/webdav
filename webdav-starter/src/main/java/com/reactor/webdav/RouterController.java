package com.reactor.webdav;

import com.reactor.webdav.dto.ParseUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.buffer.*;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.Base64Utils;
import org.springframework.web.client.HttpStatusCodeException;

import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.*;
import java.net.URI;
import java.nio.file.FileAlreadyExistsException;
import java.util.logging.Handler;


// docs
// http://www.webdav.org/specs/rfc4918.html#rfc.section.9.4
// https://learn.microsoft.com/en-us/previous-versions/office/developer/exchange-server-2003/aa142960(v=exchg.65)
// http://citforum.ru/internet/webservers/webdav/
//
// Пароль при http в windows не посылается
// https://www.asustor.com/ru/online/College_topic?topic=208
// https://blog.kharkevich.org/2014/12/webdav2-microsoft-windows.html
// https://vmblog.ru/ustanovka-klienta-webdav-v-windows-server-2016/
// https://www.pvsm.ru/nginx/67190
// https://dml.compkaluga.ru/forum/index.php?showtopic=106739
// https://arno0x0x.wordpress.com/2017/09/07/using-webdav-features-as-a-covert-channel/
// https://lists.w3.org/Archives/Public/w3c-dist-auth/2006AprJun/0023.html
@Component
@Slf4j
public class RouterController {

    @Autowired
    private PropfindToRsService toRq;

    @Autowired
    private WebDavServer server;

    @Autowired
    WebdavConfiguration cfg;

    @Autowired(required = false)
    WebDavIndexHtml indexResolver;

    final String readonlyAllow = "OPTIONS, GET, HEAD, PROPFIND, PROPPATCH, ORDERPATCH";

    @Bean
    public RouterFunctions.Builder  routeRequest(RouterController handler) {
        RouterFunctions.Builder builder = RouterFunctions.route();
        // https://www.devglan.com/spring-cloud/spring-cloud-gateway-static-content
        builder.route(new CustomHeader("OPTIONS"),   rq -> handler.interceptor(rq, handler::options));
        builder.route(new CustomHeader("PROPFIND"),  rq -> handler.interceptor(rq, handler::propfind));
        builder.route(new CustomHeader("MKCOL"),     rq -> handler.interceptor(rq, handler::mkcol));
        builder.route(new CustomHeader("GET"),       rq -> handler.interceptor(rq, handler::get));
        builder.route(new CustomHeader("HEAD"),      rq -> handler.interceptor(rq, handler::head));
        builder.route(new CustomHeader("MOVE"),      rq -> handler.interceptor(rq, handler::move));
        builder.route(new CustomHeader("PUT"),       rq -> handler.interceptor(rq, handler::put));
        builder.route(new CustomHeader("DELETE"),    rq -> handler.interceptor(rq, handler::delete));
        builder.route(new CustomHeader("COPY"),      rq -> handler.interceptor(rq, handler::copy));
        builder.route(new CustomHeader("LOCK"),      rq -> handler.interceptor(rq, handler::lock));
        builder.route(new CustomHeader("UNLOCK"),    rq -> handler.interceptor(rq, handler::unlock));
        builder.route(new CustomHeader("PROPPATCH"), rq -> handler.interceptor(rq, handler::proppatch));

        builder.route(new CustomHeader("OTHER"),    handler::other);    // заглушка для браузеров
        return builder;
    }

    @SneakyThrows
    public Mono<ServerResponse> interceptor(ServerRequest request, HandlerFunction<ServerResponse> handlerFunction) {

        // пароль прейдет только под https
        // см спецификацию http://www.webdav.org/specs/rfc4918.html#n-authentication-of-clients
        String host = request.headers().header("Host").stream().findFirst().orElse(null);
        String authorize = request.headers().header("Authorization").stream().findFirst().orElse(null);

        String user = null;
        String password = null;

        if (authorize != null && authorize.startsWith("Basic ")) {
            String[] basicArg = new String(Base64Utils.decodeFromString(authorize.replace("Basic ", ""))).split(":");
            if (basicArg.length>0) {
                user = basicArg[0];
            }
            if (basicArg.length>1) {
                password = basicArg[1];
            }
        }

        String finalUser = user;
        String finalPassword = password;

        return getAuthInfo(host, user)
                .flatMap(itemData -> {

                    if (itemData == null) {
                        return ServerResponse.status(401).build();
                    }

                    if (!equels(finalUser, itemData.getLogin()) ||
                        !equels(finalPassword, itemData.getPassword()) ) {

                        return ServerResponse.status(401).build();
                    }

                    if (itemData.getReadonly() && !readonlyAllow.contains(request.methodName().toUpperCase())) {
                        return ServerResponse.status(403).build();
                    }

                    request.exchange().getAttributes().put("config", itemData);
                    return handlerFunction.handle(request);
                })
                .onErrorResume(err -> {
                    return ServerResponse.status(401)
                            .header("WWW-Authenticate", "Basic realm=\"User Visible Realm\", charset=\"UTF-8\"").build();
                });

    }

    Mono<Item> getAuthInfo(String host, String user) {
        var res = cfg.getConfig(host, user);
        if (res == null) {
            return Mono.error(new RuntimeException("111"));
        }

        return Mono.just(res);
    }

    public Mono<ServerResponse> mkcol(ServerRequest serverRequest) {
        Item cfg = (Item) serverRequest.exchange().getAttributes().get("config");
        String rootFolder = cfg.getPath();
        return server.mkcol(rootFolder, serverRequest.uri().getPath())
            .flatMap(it -> ServerResponse.status(201).build())
            .onErrorResume(err -> ServerResponse.status(403).build())
            .onErrorResume(HttpStatusCodeException.class, err -> ServerResponse.status(err.getRawStatusCode()).build());

    }


    public Mono<ServerResponse> options(ServerRequest serverRequest) {
/*      HTTP/1.1 200 OK
        Allow: OPTIONS, GET, HEAD, POST, PUT, DELETE, TRACE, COPY, MOVE
        Allow: MKCOL, PROPFIND, PROPPATCH, LOCK, UNLOCK, ORDERPATCH
        DAV: 1, 2, ordered-collections
*/
        Item cfg = (Item) serverRequest.exchange().getAttributes().get("config");
        String allow = "OPTIONS, GET, HEAD, POST, PUT, DELETE, LOCK, TRACE, COPY, MOVE, MKCOL, PROPFIND, PROPPATCH, SEARCH, ORDERPATCH, UNLOCK";
        if (cfg != null && cfg.getReadonly()) {
            allow = readonlyAllow;
        }

        return ServerResponse.ok()
                .header("Allow", allow)
                .header("DAV", "1, 2, ordered-collections")
                .build();
    }

    // https://arno0x0x.wordpress.com/2017/09/07/using-webdav-features-as-a-covert-channel/

    public Mono<ServerResponse> propfind(ServerRequest serverRequest) {

        Item cfg = (Item) serverRequest.exchange().getAttributes().get("config");
        String rootFolder = cfg.getPath();

        String depth = getDepthHeader(serverRequest);
        Mono<String> bodyRq = null;
        // стандартный виндовый клиент не присылает тело запроса
        if (serverRequest.headers().contentLength().isPresent() && serverRequest.headers().contentLength().getAsLong() == 0) {
            bodyRq = Mono.just(ParseUtils.defaultPropfindBody);
        } else {
            bodyRq =  serverRequest.bodyToMono(String.class).onErrorReturn(ParseUtils.defaultPropfindBody);
        }


        return bodyRq.flatMap((body) -> {
                    var propfindRq = ParseUtils.parsePropfindRequest(serverRequest.uri().getPath(), depth, body);
                    return server.propfindResponse(rootFolder, propfindRq);
                })
                .map((listFiles) -> toRq.toResponseStreem(listFiles))
                .flatMap((res) -> ServerResponse.status(207).contentType(MediaType.TEXT_HTML).bodyValue(res))
                .onErrorResume(FileNotFoundException.class,   err -> ServerResponse.status(404).build())
                .onErrorResume(HttpStatusCodeException.class, err -> ServerResponse.status(err.getRawStatusCode()).build())
                .onErrorResume(err -> ServerResponse.status(403).build());

    }
    // https://github.com/nextcloud/server/blob/c9611c0f5d08cc0a41fb7018cb5691dd71b2cea0/lib/private/legacy/OC_Files.php#L352
    // https://stackoverflow.com/questions/49426304/convert-writes-to-outputstream-into-a-fluxdatabuffer-usable-by-serverresponse
    // https://www.baeldung.com/java-filechannel
    public Mono<ServerResponse> get(ServerRequest serverRequest) {

        Item cfg = (Item) serverRequest.exchange().getAttributes().get("config");
        String rootFolder = cfg.getPath();

        return server.getResource(rootFolder, serverRequest.uri().getPath())
                // BodyInserters.fromResource поддерживает заголовок Range, смотреть класс ResourceHttpMessageWriter
                .flatMap(resource -> ServerResponse.ok().body(BodyInserters.fromResource(resource)))
                .onErrorResume(NotFileException.class,   err -> {
                    if (indexResolver!=null) {
                        return indexResolver.indexHtml(serverRequest);
                    }

                    return ServerResponse.status(404).build();
                })
                .onErrorResume(FileNotFoundException.class,   err -> ServerResponse.status(404).build())
                .onErrorResume(HttpStatusCodeException.class, err -> ServerResponse.status(err.getRawStatusCode()).build())
                .onErrorResume(err -> ServerResponse.status(403).build());
    }

    public Mono<ServerResponse> head(ServerRequest serverRequest) {

        Item cfg = (Item) serverRequest.exchange().getAttributes().get("config");
        String rootFolder = cfg.getPath();

        return server.headResource(rootFolder, serverRequest.uri().getPath())
                .flatMap(resource -> ServerResponse.ok().build())
                .onErrorResume(FileNotFoundException.class,   err -> ServerResponse.notFound().build())
                .onErrorResume(HttpStatusCodeException.class, err -> ServerResponse.status(err.getRawStatusCode()).build())
                .onErrorResume(err -> ServerResponse.status(403).build());
    }

    @SneakyThrows
    public Mono<ServerResponse> move(ServerRequest serverRequest) {

        Item cfg = (Item) serverRequest.exchange().getAttributes().get("config");
        String rootFolder = cfg.getPath();

        String destination = getDestination(serverRequest);

        return  server.move(rootFolder, serverRequest.uri().getPath(), destination)
                .flatMap(resource -> ServerResponse.status(201).build())
                .onErrorResume(FileAlreadyExistsException.class, err -> ServerResponse.status(412).build())
                .onErrorResume(HttpStatusCodeException.class,    err -> ServerResponse.status(err.getRawStatusCode()).build())
                .onErrorResume(err -> ServerResponse.status(403).build());
    }

    // https://manhtai.github.io/posts/flux-databuffer-to-inputstream/
    public Mono<ServerResponse> put(ServerRequest serverRequest) {

        Item cfg = (Item) serverRequest.exchange().getAttributes().get("config");
        String rootFolder = cfg.getPath();

        // винда не присылает байты, Нужно создать пустой файл
        if (serverRequest.headers().contentLength().isPresent() && serverRequest.headers().contentLength().getAsLong() == 0) {
            return server.createEmpty(rootFolder, serverRequest.uri().getPath())
                    .flatMap(resource -> ServerResponse.status(201).build());
        }

        Flux<DataBuffer> bodyBuffer = serverRequest.body(BodyExtractors.toDataBuffers());
        return server.putFile(rootFolder, serverRequest.uri().getPath(), bodyBuffer)
                .flatMap((r) -> ServerResponse.ok().build())
                .onErrorResume(FileNotFoundException.class,   err -> ServerResponse.status(409).build())
                .onErrorResume(err -> ServerResponse.status(403).build());

    }



    public Mono<ServerResponse> delete(ServerRequest serverRequest) {

        Item cfg = (Item) serverRequest.exchange().getAttributes().get("config");
        String rootFolder = cfg.getPath();

        return server.delete(rootFolder, serverRequest.uri().getPath())
                .flatMap(resource -> ServerResponse.ok().build())
                .onErrorResume(FileNotFoundException.class,   err -> ServerResponse.notFound().build())
                .onErrorResume(HttpStatusCodeException.class, err -> ServerResponse.status(err.getRawStatusCode()).build())
                .onErrorResume(err -> ServerResponse.status(403).build());
    }

    @SneakyThrows
    public Mono<ServerResponse> copy(ServerRequest serverRequest) {

        Item cfg = (Item) serverRequest.exchange().getAttributes().get("config");
        String rootFolder = cfg.getPath();

        String depth = getDepthHeader(serverRequest);
        String destination = getDestination(serverRequest);
        boolean overwriting = getOverwritingHeader(serverRequest);

        return server.copy(rootFolder, serverRequest.uri().getPath(), destination, overwriting, depth)
                .flatMap(resource -> ServerResponse.status(204).build())
                .onErrorResume(FileAlreadyExistsException.class, err -> ServerResponse.status(412).build())
                .onErrorResume(HttpStatusCodeException.class,    err -> ServerResponse.status(err.getRawStatusCode()).build())
                .onErrorResume(err -> ServerResponse.status(403).build());
    }



    // Запрос <?xml version="1.0" encoding="utf-8" ?><D:lockinfo xmlns:D="DAV:"><D:lockscope><D:exclusive/></D:lockscope><D:locktype><D:write/></D:locktype><D:owner><D:href>alekseisosnovskikh</D:href></D:owner></D:lockinfo>
    // ответ
    // <?xml version="1.0" encoding="UTF-8"?>
    //<d:prop xmlns:d="DAV:">
    //	<d:lockdiscovery>
    //		<d:activelock>
    //			<d:lockscope>
    //				<d:exclusive/>
    //			</d:lockscope>
    //			<d:locktype>
    //				<d:write/>
    //			</d:locktype>
    //			<d:depth>infinity</d:depth>
    //			<d:timeout>Second-86400</d:timeout>
    //			<d:locktoken>
    //				<d:href>opaquelocktoken:2da16a6b-d2ef-427f-b712-7a0a144f756b</d:href>
    //			</d:locktoken>
    //		</d:activelock>
    //	</d:lockdiscovery>
    //</d:prop>
    private Mono<ServerResponse> lock(ServerRequest serverRequest) {
        Item cfg = (Item) serverRequest.exchange().getAttributes().get("config");
        String rootFolder = cfg.getPath();

        return serverRequest.bodyToMono(String.class)
            // распечатываем запрос
            .map((bodyRes) -> printRq(serverRequest, bodyRes))
            .flatMap((body) -> {
                var lockInfo = ParseUtils.parseLockInfoRequest(body);
                return server.lockFile(rootFolder, lockInfo);
            })
            .flatMap(lockData -> ServerResponse.ok()
                .contentType(MediaType.APPLICATION_XML)
                .bodyValue(ParseUtils.lockDataToBody(lockData))
            )
           .onErrorResume(UnsupportedOperationException.class, err -> ServerResponse.ok().contentType(MediaType.APPLICATION_XML).build());

    }


    /*
    PROPPATCH /test/EULA.pdf
    If: [(<opaquelocktoken:2da16a6b-d2ef-427f-b712-7a0a144f756b>)]

    <?xml version="1.0" encoding="utf-8" ?><D:propertyupdate xmlns:D="DAV:" xmlns:Z="urn:schemas-microsoft-com:"><D:set><D:prop><Z:Win32LastModifiedTime>Tue, 16 Apr 2024 15:16:27 GMT</Z:Win32LastModifiedTime><Z:Win32FileAttributes>00000020</Z:Win32FileAttributes></D:prop></D:set></D:propertyupdate>
    * */
    public Mono<ServerResponse> unlock(ServerRequest serverRequest) {

        Mono<String> bodyRq = null;
        if (serverRequest.headers().contentLength().isPresent() && serverRequest.headers().contentLength().getAsLong()==0) {
            bodyRq = Mono.just("");
        } else {
            bodyRq =  serverRequest.bodyToMono(String.class).onErrorReturn(ParseUtils.defaultPropfindBody);
        }

        return bodyRq.flatMap(bodyRqData -> {
            StringBuilder sb = new StringBuilder();
            sb.append("\n"+serverRequest.exchange().getRequest().getMethodValue() + " " + serverRequest.path()).append("\n");
            serverRequest.headers().asHttpHeaders().forEach((itKey, itVal) -> {
                sb.append(itKey + ": " + itVal + "\n");
            });

            if (!bodyRqData.isEmpty()) {
                sb.append("\n").append(bodyRqData);
            }

            log.info(sb.toString());
            return ServerResponse.status(204).build();
        });

    }

    /*
    rq
    PROPPATCH /New%20folder/EULA.pdf
    If: [(<opaquelocktoken:2da16a6b-d2ef-427f-b712-7a0a144f756b>)]
    translate: [f]
    Content-Length: [443]

    <?xml version="1.0" encoding="utf-8" ?>
    <D:propertyupdate xmlns:D="DAV:" xmlns:Z="urn:schemas-microsoft-com:">
        <D:set>
            <D:prop>
                <Z:Win32CreationTime>Tue, 16 Apr 2024 15:16:27 GMT</Z:Win32CreationTime>
                <Z:Win32LastAccessTime>Sat, 08 Jun 2024 12:41:45 GMT</Z:Win32LastAccessTime>
                <Z:Win32LastModifiedTime>Tue, 16 Apr 2024 15:16:27 GMT</Z:Win32LastModifiedTime>
                <Z:Win32FileAttributes>00000000</Z:Win32FileAttributes>
            </D:prop>
        </D:set>
    </D:propertyupdate>
    rs
    <?xml version="1.0" encoding="UTF-8"?>
    <d:multistatus xmlns:d="DAV:">
        <d:response>
            <d:href>/%D0%97%D0%B0%D0%B3%D1%80%D1%83%D0%B7%D0%BA%D0%B8/EULA.pdf</d:href>
            <d:propstat>
                <d:prop>
                    <Win32FileAttributes xmlns="urn:schemas-microsoft-com:">00000000</Win32FileAttributes>
                </d:prop>
                <d:status>HTTP/1.1 200 OK</d:status>
            </d:propstat>
        </d:response>
    </d:multistatus>
    */
    public Mono<ServerResponse> proppatch(ServerRequest serverRequest) {
        Mono<String> bodyRq = null;
        if (serverRequest.headers().contentLength().isPresent() && serverRequest.headers().contentLength().getAsLong()==0) {
            bodyRq = Mono.just("");
        } else {
            bodyRq =  serverRequest.bodyToMono(String.class).onErrorReturn(ParseUtils.defaultPropfindBody);
        }

        return bodyRq
            // распечатываем запрос
            .map((bodyRes) -> printRq(serverRequest, bodyRes))
            .flatMap(bodyRqData -> {

                    return ServerResponse.status(207).bodyValue(
                            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                            "<d:multistatus xmlns:d=\"DAV:\">\n" +
                            "        <d:response>\n" +
                            "            <d:href>/%D0%97%D0%B0%D0%B3%D1%80%D1%83%D0%B7%D0%BA%D0%B8/EULA.pdf</d:href>\n" +
                            "            <d:propstat>\n" +
                            "                <d:prop>\n" +
                            "                    <Win32FileAttributes xmlns=\"urn:schemas-microsoft-com:\">00000000</Win32FileAttributes>\n" +
                            "                </d:prop>\n" +
                            "                <d:status>HTTP/1.1 200 OK</d:status>\n" +
                            "            </d:propstat>\n" +
                            "        </d:response>\n" +
                            "    </d:multistatus>"
                    );
        });

    }


    String printRq(ServerRequest serverRequest, String bodyRes) {
        if (!log.isTraceEnabled()) {
            return bodyRes;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("\n" + serverRequest.exchange().getRequest().getMethodValue() + " " + serverRequest.path()).append("\n");
        serverRequest.headers().asHttpHeaders().forEach((itKey, itVal) -> {
            sb.append(itKey + ": " + itVal + "\n");
        });
        sb.append("\n");
        sb.append(bodyRes);
        log.trace(sb.toString());
        return bodyRes;
    }


    private Mono<ServerResponse> other(ServerRequest serverRequest) {
        log.info("other " + serverRequest.methodName());
        Mono<String> bodyRq = null;
        if (serverRequest.headers().contentLength().isPresent() && serverRequest.headers().contentLength().getAsLong()==0) {
            bodyRq = Mono.just("");
        } else {
            bodyRq =  serverRequest.bodyToMono(String.class).onErrorReturn(ParseUtils.defaultPropfindBody);
        }

        return bodyRq.flatMap(bodyRqData -> {
            StringBuilder sb = new StringBuilder();
            sb.append("\n"+serverRequest.exchange().getRequest().getMethodValue() + " " + serverRequest.path()).append("\n");
            serverRequest.headers().asHttpHeaders().forEach((itKey, itVal) -> {
                sb.append(itKey + ": " + itVal + "\n");
            });

            if (!bodyRqData.isEmpty()) {
                sb.append("\n").append(bodyRqData);
            }

            log.info(sb.toString());
            return ServerResponse.status(404).build();
        });

    }

    @SuppressWarnings("all")
    private Boolean equels(Object a, Object b){
        if (a == b)
            return true;
        if ((a == null || "".equals(a)) && (b == null || "".equals(b)))
            return true;
        if (a != null)
            return a.equals(b);

        if (b != null)
            return b.equals(b);

        return false;
    }

    private boolean getOverwritingHeader(ServerRequest serverRequest){
        String overwritingHeader = serverRequest.headers().firstHeader("Overwrite");
        boolean overwriting = false;
        if (overwritingHeader != null) {
            overwriting = overwritingHeader.equals("T");
        }
        return overwriting;

    }
    private String getDepthHeader(ServerRequest serverRequest){
        String depth = serverRequest.headers().firstHeader("Depth");
        if (depth == null || depth.isEmpty()) {
            depth = "infinity";
        }
        return depth;
    }

    @SneakyThrows
    private String getDestination(ServerRequest serverRequest){
        String destination = serverRequest.headers().firstHeader("Destination");
        assert destination != null;
        destination = new URI(destination).getPath();
        return destination;
    }

}

