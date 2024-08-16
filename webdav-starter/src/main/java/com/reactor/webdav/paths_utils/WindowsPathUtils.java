package com.reactor.webdav.paths_utils;

import com.reactor.webdav.conditionals.IsWindowsCondition;
import org.springframework.context.annotation.Conditional;

import org.springframework.stereotype.Component;

import java.io.File;

@Component
@Conditional({IsWindowsCondition.class})
public class WindowsPathUtils implements PathUtils {

    @Override
    public File toFile(String rootFolder, String requestPath) {
        return  new File(rootFolder + requestPath.replaceAll("/", "\\\\"));
    }
}
