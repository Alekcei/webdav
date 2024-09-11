package com.reactor.webdav.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Locale;

@Builder
@Data
public class LockInfo {
    Scope scope; // exclusive | shared
    LockType type;
    String owner;

    public static enum Scope {
        exclusive,
        shared
    }

    static enum LockType {
        write,
    }
}
