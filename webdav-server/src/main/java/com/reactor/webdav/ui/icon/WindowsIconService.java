package com.reactor.webdav.ui.icon;

import com.reactor.webdav.conditionals.IsWindowsCondition;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

// TODO: IMPL
@Service
@Conditional({IsWindowsCondition.class})
public class WindowsIconService implements IconService {
    @Override
    public Resource getIconFromExtension(String extension) {
        return null;
    }
}
