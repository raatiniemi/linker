FROM openjdk:8-jre-slim-buster
LABEL maintainer="Tobias Raatiniemi <raatiniemi@gmail.com>"

RUN set -x \
  && mkdir -p /etc/linker \
  && mkdir -p /opt/linker

ARG JAR_FILE=build/libs/*-all.jar
COPY ${JAR_FILE} /opt/linker/app.jar

VOLUME ["/etc/linker"]

ENTRYPOINT ["java", "-jar", "/opt/linker/app.jar", "/etc/linker/configuration.json"]
