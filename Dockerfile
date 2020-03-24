FROM camunda/zeebe:0.22.1

COPY target/exporter-*.jar /usr/local/zeebe/lib/
COPY kafka-exporter.toml /tmp/
RUN cat /tmp/kafka-exporter.toml >> /usr/local/zeebe/conf/zeebe.cfg.toml



