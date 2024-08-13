package ru.dump.response.configure;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.helper.FileNamePattern;
import ch.qos.logback.core.util.FileSize;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.stereotype.Component;
import reactor.netty.http.server.logging.AccessLog;
import reactor.netty.http.server.logging.AccessLogArgProvider;
import reactor.netty.http.server.logging.AccessLogFactory;

import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;

@Component
@ConditionalOnProperty(prefix = "logging.access", name = "name")
public class NettyAccessLogCustomizer implements WebServerFactoryCustomizer<NettyReactiveWebServerFactory> {

    @Value("${logging.access.name:./access.log}")
    private String accessFileName;

    @Value("${logging.access.rollingpolicy.clean-history-on-start:false}")
    private boolean clearOnStart;


    @Value("${logging.access.rollingpolicy.file-name-pattern:0}")
    private String fileNamePattern;

    @Value("${logging.access.rollingpolicy.max-history:7}")
    private int maxHistory;

    @Value("${logging.access.rollingpolicy.total-size-cap:0}")
    private String sizeCap;

    @Value("${logging.access.rollingpolicy.max-file-size:100MB}")
    private String maxFileSize;

    @Value("${logging.access.pattern:{ip} {accessTime} {method} {path} {referUri} {status} {duration}}")
    private String pattern;

    final AtomicReference<Object> at = new AtomicReference<>();
    final AtomicReference<Object> atKeys = new AtomicReference<>();
    @Override
    public void customize(NettyReactiveWebServerFactory factory) {

        // https://akhikhl.wordpress.com/2013/07/11/programmatic-configuration-of-slf4jlogback/
        // https://projectreactor.io/docs/netty/release/reference/index.html#_http_access_log

        // LoggerContext logCtx = LoggerFactory.getILoggerFactory();

        String regex = "(\\{([^}]+)\\})";
        List<String> matches = new ArrayList<>();
        Matcher m = Pattern.compile(regex).matcher(pattern);
        String msgPattern = pattern;

        while (m.find()) {

            msgPattern = msgPattern.replace(m.group(1), "{}");
            matches.add(m.group(2));
        }

        atKeys.set(matches);
        at.set(msgPattern);

        createLoggerFor("reactor.netty.http.server.AccessLog", accessFileName);
        factory.addServerCustomizers(server ->
            server.accessLog(true, AccessLogFactory.createFilter(
                    p -> true,
                    x -> AccessLog.create((String) at.get(),
                            getArg(x)
                    )
                )
            )
        );
    }

    private Logger createLoggerFor(String string, String file) {
        // https://stackoverflow.com/questions/5863054/rollingfileappender-set-rollingpolicy-programmatically
        // https://stackoverflow.com/questions/46263034/timebasedrollingpolicy-logback-programmatically
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        PatternLayoutEncoder ple = new PatternLayoutEncoder();
        ple.setPattern("%msg%n");
        ple.setContext(lc);
        ple.start();
//        FileAppender<ILoggingEvent> fileAppender = new FileAppender<>();
        RollingFileAppender<ILoggingEvent> fileAppender = new RollingFileAppender<>();
        // var policy = new ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy<>();
        var policy = new ch.qos.logback.core.rolling.TimeBasedRollingPolicy<>();

        policy.setContext(lc);
        policy.setFileNamePattern(file + ".%d{yyyy-MM-dd}.gz");
        // policy.setMaxFileSize(FileSize.valueOf(maxFileSize));
        policy.setTotalSizeCap(FileSize.valueOf(sizeCap));
        policy.setMaxHistory(maxHistory);
        policy.setCleanHistoryOnStart(clearOnStart);
        policy.setParent(fileAppender);
        policy.start();

        fileAppender.setRollingPolicy(policy);
        fileAppender.setFile(file);
        fileAppender.setEncoder(ple);
        fileAppender.setContext(lc);

        fileAppender.start();

        var asyncAppender = new ch.qos.logback.classic.AsyncAppender();
        asyncAppender.addAppender(fileAppender);
        asyncAppender.setContext(lc);
        asyncAppender.start();

        Logger logger = (Logger) LoggerFactory.getLogger(string);
        logger.addAppender(asyncAppender);
        logger.setLevel(Level.DEBUG);
        logger.setAdditive(false);
        return logger;
    }


    @SuppressWarnings({"unchecked", "ConstantConditions"})
    Object[] getArg(AccessLogArgProvider arg) {

        List<String> keys =  ((List<String>) atKeys.get());
        Object[] allArg = new Object[keys.size()];
        int i = -1;
        for (String key : keys) {
            i++;
            if ("ip".equals(key)) {
                SocketAddress ipAddr = arg.connectionInformation().connectionRemoteAddress();
                // https://stackoverflow.com/questions/22690907/client-socket-get-ip-java
                if (ipAddr instanceof InetSocketAddress) {
                    InetAddress intAddr = ((InetSocketAddress) ipAddr).getAddress();
                    allArg[i] = intAddr.getHostAddress();
                } else {
                    allArg[i] = arg.connectionInformation().connectionRemoteAddress().toString();
                }

                continue;
            }

            if ("method".equals(key)) {
                allArg[i] = arg.method();
                continue;
            }

            if ("path".equals(key)) {
                allArg[i] = arg.uri();
                continue;
            }

            if ("status".equals(key)) {
                allArg[i] = arg.status();
                continue;
            }

            if ("duration".equals(key)) {
                allArg[i] = arg.duration();
                continue;
            }

            if ("accessTime".equals(key)) {
                allArg[i] = arg.accessDateTime().toLocalDateTime().toString();
                continue;
            }

            if ("refer".equals(key)) {
                try {
                allArg[i] = arg.requestHeader("Referer");
                } catch (Exception e) {
                    allArg[i] = "-";
                }
                continue;
            }

            if ("referUri".equals(key)) {
                try {
                    allArg[i] = new URL(arg.requestHeader("Referer").toString()).getPath();
                } catch (Exception e) {
                    allArg[i] = "-";
                }
                continue;
            }

            allArg[i] = "-";
        }

        return allArg;
    }
}
