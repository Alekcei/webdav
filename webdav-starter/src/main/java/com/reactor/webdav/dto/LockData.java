package com.reactor.webdav.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class LockData {
    LockInfo.Scope scope; // exclusive | shared
    LockInfo.LockType type;
    String   owner;
    String timeout;
    String   depth;
    String token;

}
