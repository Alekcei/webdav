package com.reactor.webdav.ui.icon;

import com.reactor.webdav.conditionals.IsLinuxCondition;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Map;

@Service
@Conditional({IsLinuxCondition.class})
public class StaticIconService implements IconService {

    String iconFolder;
    @PostConstruct
    void init(){
        iconFolder = "/usr/share/icons";
    }


    Map<String, String> reMap = Map.of(
            "text-javascript", "application-javascript",
            "application-gzip","package",
            "application-zip","package",
            "application-x-7z-compressed","package",
            "application-java-archive", "application-x-java-archive",
            "image-svg+xml", "image-x-svg+xml",
            "video-x-matroska", "application-x-matroska",
            "", "application-octet-stream"
    );
    @Override
    public Resource getIconFromExtension(String mimetype) {
        var arg = mimetype.split("/");
        mimetype = mimetype.replaceAll("/", "-");
        if (mimetype.contains("folder")) {
            return new FileSystemResource(iconFolder+"/Mint-Y/places/64/folder.png");
        }

        Resource mimeIconFolder = new FileSystemResource(iconFolder+"/Mint-Y/mimetypes/64/" + mimetype + ".png");
        if (!mimeIconFolder.exists()) {
            mimeIconFolder = new FileSystemResource(iconFolder+"/Mint-Y/mimetypes/64/" + reMap.get(mimetype) + ".png");
        }

        if (!mimeIconFolder.exists() && arg.length>0) {
            mimeIconFolder = new FileSystemResource(iconFolder+"/Mint-Y/mimetypes/64/" + arg[0] + ".png");


            if (!mimeIconFolder.exists()) {
                mimeIconFolder = new FileSystemResource(iconFolder+"/Mint-Y/mimetypes/64/" + arg[0]+"-x-generic" + ".png");
            }
        }

        if (mimeIconFolder.exists()) {
            return mimeIconFolder;
        }
        // "/usr/share/icons/Mint-Y/mimetypes/64";
        return new FileSystemResource("/home/alekcei/example.jpg");
    }
}
