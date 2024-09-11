package com.reactor.webdav;

import com.reactor.webdav.dto.LockData;
import com.reactor.webdav.dto.LockInfo;
import com.reactor.webdav.dto.Propfind;
import org.springframework.core.io.Resource;
import org.springframework.core.io.buffer.DataBuffer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.List;

public interface WebDavServer {

    Mono<List<FileInfo>> propfindResponse(String rootFolder, Propfind r);

    Mono<Boolean> mkcol(String rootFolder, String uriPath);

    Mono<Boolean> headResource(String rootFolder, String path);

    Mono<Resource> getResource(String rootFolder, String path);

    Mono<Void> putFile(String rootFolder, String rqPath, Flux<DataBuffer> bodyBuffer);

    Mono<Boolean> copy(String rootFolder, String source, String target, boolean overwriting, String depth);

    Mono<Boolean> delete(String rootFolder, String path);

    Mono<Boolean> move(String rootFolder, String source, String target);

    default Mono<Void> createEmpty(String rootFolder, String rqPath) {
        return Mono.empty();
    }

    // Блокировка файла
    default Mono<LockData> lockFile(String rootFolder, LockInfo lockInfo) {

        return Mono.just(
            LockData.builder()
                    .scope(lockInfo.getScope())
                    .type(lockInfo.getType())
                    .depth("infinity")
                    .timeout("Second-86400")
                    .token("opaquelocktoken:2da16a6b-d2ef-427f-b712-7a0a144f756b")
                    .build()
        );
    }

    // Разблокировка файла
    default Mono<LockData> unlockFile(String rootFolder, LockInfo lockInfo) {

        return Mono.error(new UnsupportedOperationException());
    }
}
