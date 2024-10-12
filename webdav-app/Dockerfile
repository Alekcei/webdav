FROM openjdk:11
COPY webdav.jar app/webdav.jar
COPY application.* app/
COPY front app/front
WORKDIR "/app"
EXPOSE 8080/tcp
VOLUME "/volume"
ENTRYPOINT ["java", "-jar", "webdav.jar", "--webdav.folder.path=/volume"]