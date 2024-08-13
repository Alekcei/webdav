package com.reactor.webdav.paths_utils;


import com.reactor.webdav.conditionals.IsLinuxCondition;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
@Conditional({IsLinuxCondition.class})
public class LinuxPathUtils implements PathUtils {

    @Override
    public File toFile(String rootFolder, String requestPath) {
        return new File(rootFolder + requestPath);
    }
}
