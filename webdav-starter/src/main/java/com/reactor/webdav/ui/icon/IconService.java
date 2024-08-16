package com.reactor.webdav.ui.icon;

import org.springframework.core.io.Resource;

public interface IconService {

    Resource getIconFromExtension(String extension);


//    public default byte[] getIconFromExtension(String extension){
//
//        return new byte[0];
//    }
}
