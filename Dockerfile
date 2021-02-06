FROM openjdk:15-alpine
LABEL maintainer="Tobias Raatiniemi <raatiniemi@gmail.com>"

RUN set -x \
  && mkdir -p /etc/linker \
  && mkdir -p /opt/linker

ARG JAR_FILE=cli/build/libs/*-all.jar
COPY ${JAR_FILE} /opt/linker/app.jar

VOLUME ["/etc/linker"]

ENTRYPOINT ["java", "-jar", "/opt/linker/app.jar", "-c", "/etc/linker/configuration.json"]
