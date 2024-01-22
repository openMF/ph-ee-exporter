FROM camunda/zeebe:8.3.5
USER root
RUN apt update; apt install -y iputils-ping vim telnet less; apt clean
USER zeebe:zeebe
ENV ZEEBE_BROKER_EXPORTERS_KAFKA_JARPATH exporters/phee-exporter.jar
ENV ZEEBE_BROKER_EXPORTERS_KAFKA_CLASSNAME hu.dpc.rt.kafkastreamer.exporter.KafkaExporter
ENV ZEEBE_KAFKAEXPORT_URL bitnami-kafka:9092
ADD target/exporter*.jar /usr/local/zeebe/exporters/phee-exporter.jar


