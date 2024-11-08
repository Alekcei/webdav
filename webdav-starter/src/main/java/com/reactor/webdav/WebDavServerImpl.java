package com.reactor.webdav;

import com.reactor.webdav.dto.Propfind;
import com.reactor.webdav.paths_utils.PathUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import org.springframework.util.FileSystemUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.FileNotFoundException;

import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.nio.file.attribute.FileTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

// file webdav
@Component
@Slf4j
public class WebDavServerImpl implements WebDavServer {

    @Autowired
    PathUtils pathsUtils;

    private List<String> supported = List.of( "getcontentlength", "resourcetype",
            "quota-available-bytes", "quota-used-bytes",
            "creationdate", "getcontenttype",
            "displayname",  "getetag", "getlastmodified",
            "executable"
            // "bigbox", "author", "editor",
    );

    @Override
    public  Mono<List<FileInfo>> propfindResponse(String rootFolder, Propfind r) {


        List<String> props = r.getProps();
        if (r.isAllProp() || r.isPropName()) {
            props = supported;
        }
        List<String> unSupported = new LinkedList<>();
        props.forEach(it -> {
            if(!supported.contains(it))  unSupported.add(it);
        });

        if (unSupported.size() > 0) {
            log.info("unSupported  " + unSupported);
        }

        List<FileInfo> fileProps = new LinkedList<>();
        var path =  r.getPath();
        if (File.separator.equals("\\")) {
            path = path.replaceAll("/", "\\\\");
        }

        File file = new File(rootFolder + File.separator + path);

        FileInfo fileProp = fileProp(rootFolder, file, props);
        if (!fileProp.exist) {
            return Mono.error(new FileNotFoundException());
        }
        fileProps.add(fileProp);
        // depth = 1 то текущий каталог
        if (fileProp.exist && (r.isPropName() || r.getDepth().equals("1")) && file.isDirectory()) {
            for (File childFile : Objects.requireNonNull(file.listFiles())) {
                FileInfo fileProp2 = fileProp(rootFolder, childFile, props);
                fileProps.add(fileProp2);
            }
        }
        if (fileProps.isEmpty()) {
            return Mono.error(new FileNotFoundException());
        }

        return Mono.just(fileProps);

    }


    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public Mono<Boolean> mkcol(String rootFolder, String uriPath) {
        File fileLink = new File(rootFolder + uriPath);
        if (fileLink.exists()) {
            return Mono.error(new FileNotFoundException());
        }
        fileLink.mkdir();
        return  Mono.just(true);
    }

    @Override
    public Mono<Boolean> headResource(String rootFolder, String path) {

        File fileLink = new File(rootFolder + path);
        if (!fileLink.exists()) {
            return Mono.error(new FileNotFoundException());
        }

        return Mono.just(true);
    }

    @Override
    public  Mono<Resource>getResource(String rootFolder, String path) {
        File fileLink = new File(rootFolder + path);

        if (!fileLink.exists()) {
            return Mono.error(new FileNotFoundException());
        }

        if (fileLink.isDirectory()) {
            return Mono.error(new NotFileException());
        }

        Resource resource = new FileSystemResource(fileLink);
        return Mono.just(resource);
    }

    public Mono<Void> putFile(String rootFolder, String rqPath, Flux<DataBuffer> bodyBuffer){
        File fileLink = new File(rootFolder + rqPath);
        Path path = Paths.get(fileLink.getPath());
        if (!path.getParent().toFile().exists()) {
            return Mono.error(new FileNotFoundException()); //ServerResponse.status(409).build();
        }

        return DataBufferUtils.write(bodyBuffer, path);
    }

    @Override
    @SneakyThrows
    public Mono<Boolean> copy(String rootFolder, String source, String target, boolean overwriting, String depth) {

        File sourceFile = pathsUtils.toFile(rootFolder, source); // ;  new File(root + source);
        File targetFile = pathsUtils.toFile(rootFolder, target);
        if (targetFile.exists()) {
            if(overwriting) {
                targetFile.deleteOnExit();
            } else {
                return Mono.error(new FileAlreadyExistsException(target)); // файл уже существует
            }
        }

        FileCopyUtils.copy(sourceFile, targetFile);
        return  Mono.just(true);
    }

    @Override
    @SneakyThrows
    public Mono<Boolean> delete(String rootFolder, String path) {

        File fileLink = new File(rootFolder + path);
        if (!fileLink.exists()) {
            return Mono.error(new FileNotFoundException());
        }

        var resDel = false;
        if (fileLink.isDirectory()) {
            resDel = FileSystemUtils.deleteRecursively(new File("testfolder/webdav1"));
        } else  {
            resDel = fileLink.delete();
        }

        if (!resDel) {
            return Mono.error(new FileNotFoundException());
        }

        return Mono.just(true);
    }
    @Override
    @SneakyThrows
    public Mono<Boolean> move(String rootFolder, String source, String target) {

        // target = new URL(target).getPath();
        File sourceFile = pathsUtils.toFile(rootFolder, source);
        sourceFile.renameTo( pathsUtils.toFile(rootFolder, target));

        return  Mono.just(true);
    }


    @SneakyThrows
    private String encodePath(String path) {
        URI url = new URI("http", "localhost", path, null);
        path = url.toASCIIString().replaceAll("http://localhost", "");
        return path;
    }


    @SneakyThrows
    private FileInfo fileProp(String rootFolder, File file, List<String> props) {

        String href = file.toURI().getPath().replaceAll(new File(rootFolder).getCanonicalFile().toURI().getPath(), "/")
                .replaceAll("//", "/");


        if (file.isDirectory() && !href.endsWith("/")) {
            href += "/";
        }

        href = encodePath(href);

        var builderInfo = FileInfo.builder();
        builderInfo.href(href);
        if (!file.exists()) {
            return builderInfo.exist(false).build();
        }

        builderInfo.exist(file.exists());
        props.forEach(it -> {

            if (it.equals("displayname")) {
                builderInfo.displayname(file.getName());
                return;
            }

            if (it.equals("getetag")) {
                builderInfo.etag("");
                return;
            }

            if (it.equals("getcontentlength")) {
                builderInfo.contentLength(file.length());
                return;
            }

            if (it.equals("creationdate")) {

                try {
                    FileTime creationTime = (FileTime) Files.getAttribute(file.toPath(), "creationTime");
                    builderInfo.creationdate(creationTime.toMillis());
                } catch (IOException ignore) {
                    // handle exception
                }

                return;
            }

            if (it.equals("lastmodified")) {
                builderInfo.lastmodified(file.lastModified());
                return;
            }
            if (it.equals("executable")) {
                builderInfo.executable(file.canExecute());
                return;
            }

            if (it.equals("resourcetype")) {
                if (file.isDirectory()) {
                    builderInfo.isCollection(true);
                }
                return;
            }

            if (it.equals("getcontenttype") && !file.isDirectory()) {
                try {
                    builderInfo.mimeType(Files.probeContentType(file.toPath()));
                } catch (IOException ignore) {
                    WebDavServerImpl.log.info("MimeType error for " + file.getName());
                }
            }

            if (it.equals("quota-available-bytes")) {
                try {
                    FileStore store = Files.getFileStore(new File(rootFolder).toPath());
                    builderInfo.availbleBytes(store.getTotalSpace() - store.getUsableSpace());
                } catch (IOException ignore) {
                }

                return;
            }

            if (it.equals("quota-used-bytes")) {

                try {
                    FileStore store = Files.getFileStore(new File(rootFolder).toPath());
                    builderInfo.usedBytes(store.getUsableSpace());
                } catch (IOException ignore) {
                }

                return;
            }

        });
        return builderInfo.build();
    }
}
