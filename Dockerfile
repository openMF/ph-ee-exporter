FROM camunda/zeebe:8.2.12

COPY target/exporter-1.0.0-SNAPSHOT.jar /exporters/ph-ee-kafka-exporter.jar