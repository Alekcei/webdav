package com.reactor.webdav.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ParseUtils {

    static XmlMapper xmlMapper = new XmlMapper();
    public static final String defaultPropfindBody = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n" +
            " <D:propfind xmlns:D=\"DAV:\">\n" +
            "  <D:prop>\n" +
            "<D:creationdate/>\n" +
            "<D:displayname/>\n" +
            "<D:getcontentlength/>\n" +
            "<D:getcontenttype/>\n" +
            "<D:getetag/>\n" +
            "<D:getlastmodified/>\n" +
            "<D:resourcetype/>\n" +
            "  </D:prop>\n" +
            " </D:propfind>";

    public static Propfind parsePropfindRequest(String path, String depth, String body) {

        Map<?, ?> value;
        try {
            value = xmlMapper.readValue(body, LinkedHashMap.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        List<String> props = null;
        if (value.containsKey("prop")) {
            List<String> _props = new LinkedList<>();
            ((Map<?, ?>)value.get("prop")).keySet().forEach(it -> _props.add(it.toString()) );
            props = _props;
        }

        return Propfind.builder()
                .path(path)
                .depth(depth)
                .props(props)
                .allProp(value.containsKey("allprop"))
                .propName(value.containsKey("propname"))
                .build();
    }

    @SuppressWarnings({"all"})
    public static LockInfo parseLockInfoRequest(String body) {
        Map<String, Map<?,?>> value;
        try {
            value = xmlMapper.readValue(body, LinkedHashMap.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        LockInfo.Scope scope = LockInfo.Scope.shared;;
        if (value.get("lockscope").containsKey("exclusive")) {
            scope = LockInfo.Scope.exclusive;
        }
        LockInfo.LockType type = LockInfo.LockType.write;
        if (value.get("locktype").containsKey("write")) {
            type = LockInfo.LockType.write;
        }
        String owner = (String)(value.getOrDefault("owner", Map.of()).get("href"));

        return LockInfo.builder()
                .scope(scope)
                .type(type)
                .owner(owner)
                .build();
    }

    public static String lockDataToBody(LockData lock) {
        var body = new StringBuilder();
        body.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
            .append("<d:prop xmlns:d=\"DAV:\">")
            .append("<d:lockdiscovery>")
            .append("<d:activelock>");
        // scope
        body.append("<d:lockscope>");
        if (lock.getScope().equals(LockInfo.Scope.exclusive)) {
            body.append("<d:exclusive/>");
        } else {
            body.append("<d:shared/>");
        }
        body.append("</d:lockscope>");

        // locktype
        body.append("<d:locktype>");
        if (lock.getType().equals(LockInfo.LockType.write)) {
            body.append("<d:write/>");
        }
        body.append("</d:locktype>");

        // depth
        if (lock.getDepth() != null) {
            body.append("<d:depth>").append(lock.getDepth()).append("</d:depth>");
        }

        // timeout
        if (lock.getTimeout() != null) {
            body.append("<d:timeout>").append(lock.getTimeout()).append("</d:timeout>");
        }

        // locktoken
        body.append("<d:locktoken>");
        if (lock.getToken() != null) {
            body.append("<d:href>").append(lock.getToken()).append("</d:href>");
        }
        body.append("</d:locktoken>");

        // end
        body.append("</d:activelock>")
            .append("</d:lockdiscovery>")
            .append("</d:prop>");

        return body.toString();
    }


}
