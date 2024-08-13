package com.reactor.webdav.client;

public enum MethodType {
    OPTIONS,
    HEAD,
    PUT,
    GET,
    POST,
    COPY,
    DELETE,
    LOCK,    // блокировка файла
    UNLOCK,  // разблокировка
    TRACE,
    MOVE,
    MKCOL,     // создание папки
    PROPFIND,
    PROPPATCH,
    SEARCH,
    ORDERPATCH,
}
