FROM camunda/zeebe:0.23.0-alpha2

COPY target/exporter-*.jar /usr/local/zeebe/lib/
COPY zeebe.cfg.yaml /usr/local/zeebe/config/
COPY kafka-clients-2.4.0.jar /usr/local/zeebe/lib/


