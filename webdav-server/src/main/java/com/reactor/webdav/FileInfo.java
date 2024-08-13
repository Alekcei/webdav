package com.reactor.webdav;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class FileInfo {
    public Long availbleBytes;
    public Long usedBytes;
    public String etag;
    public Long lastmodified;
    String href;
    boolean exist;
    boolean isCollection;
    boolean executable;
    String mimeType;
    String displayname;
    Long contentLength;

    Long creationdate;
}
