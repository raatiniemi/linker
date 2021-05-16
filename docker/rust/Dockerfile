FROM alpine:latest
LABEL maintainer="Tobias Raatiniemi <raatiniemi@gmail.com>"

RUN set -x \
  && mkdir -p /etc/linker \
  && mkdir -p /opt/linker

COPY target/release/app /opt/linker/app

ENTRYPOINT ["/opt/linker/app"]

VOLUME ["/etc/linker"]

CMD ["-c", "/etc/linker/configuration.json"]
