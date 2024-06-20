FROM registry.gitlab.com/rahome/docker/debian:bookworm-slim
LABEL maintainer="Tobias Raatiniemi <raatiniemi@gmail.com>"

RUN set -x \
  && mkdir -p /etc/linker \
  && mkdir -p /opt/linker

COPY target/x86_64-unknown-linux-gnu/release/cli /opt/linker/cli

ENTRYPOINT ["/opt/linker/cli"]

VOLUME ["/etc/linker"]

CMD ["-c", "/etc/linker/configuration.json"]
