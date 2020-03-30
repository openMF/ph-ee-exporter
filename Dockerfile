FROM camunda/zeebe:0.23.0-alpha2

COPY target/exporter-*.jar /usr/local/zeebe/lib/
COPY zeebe.cfg.yaml /usr/local/zeebe/config/



