package com.reactor.webdav.paths_utils;

import java.io.File;

public interface PathUtils {
   File toFile(String rootFolder,  String requestPath);
}
