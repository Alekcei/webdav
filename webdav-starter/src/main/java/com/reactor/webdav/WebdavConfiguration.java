package com.reactor.webdav;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


import java.io.File;
import java.util.*;

@Component
@ConfigurationProperties(prefix="webdav")
@Data
public class WebdavConfiguration {

    Item folder;
    List<Item> folders;
    Map<String, Boolean> withAuth;
    Map<String, Item> byHash;

    public void setFolders(List<Item> cfgSpa) {
        folders = cfgSpa;
    }


    @PostConstruct
    void postConstruct() {
        if (folder != null) {
            if (folders == null) {
                folders = new LinkedList<>();
            }
            folders.add(folder);
        }
        if (folders == null || folders.isEmpty()) {
            return;
        }

        Map<String, Boolean> withAuth = new HashMap<>();
        Map<String, Item> byHash = new HashMap<>();
        folders.forEach(it -> {
            String host = it.getHost();
            if(host == null || host.isEmpty()) {
                host = "-";
            }

            if (it.getPassword() != null && it.getLogin()!= null) {
                withAuth.put(host , true);
            }

            byHash.put(host + "/" + it.getLogin(), it);
        });

        this.withAuth = withAuth;
        this.byHash = byHash;

    }

    public boolean needAuth(String host) {
        if (host == null || host.isEmpty()) {
            host = "-";
        }
        return this.withAuth.getOrDefault(host, this.withAuth.getOrDefault("-", false));
    }

    public Item getConfig(String host, String user) {
        if(host == null || host.isEmpty()) {
            host = "-";
        }
        return byHash.getOrDefault(host + "/" +user,
               byHash.getOrDefault(String.format("%s/%s", "-", user), null));
    }
}

@Data
// @Builder
@NoArgsConstructor
@AllArgsConstructor
class Item {

    private String  path;
    private String  host;
    private Boolean readonly = false;
    private String  login;
    private String  password;

    public void setPath(String path) {
        // для windows делаем преобразование обратного слеша для удобства
        if (File.separator.equals("\\")) {
            path = path.replaceAll("/", "\\\\");
        }
        this.path = path;
    }

    public void setHost(String host) {
        if (host == null) return;
        this.host = host.replaceAll("https+://", "");
    }
}