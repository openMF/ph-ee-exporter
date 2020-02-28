/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Zeebe Community License 1.0. You may not use this file
 * except in compliance with the Zeebe Community License 1.0.
 */
package hu.dpc.rt.kafkastreamer.exporter;

import io.zeebe.protocol.record.Record;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class KafkaExporterClient {
    private final Logger logger;
    private final KafkaExporterConfiguration configuration;
    private KafkaExporterMetrics metrics;
    private AtomicLong sentToKafka = new AtomicLong(0);

    // json content for now
    private KafkaProducer<String, String> producer;
    public static final String kafkaTopic = "zeebe-export";

    public KafkaExporterClient(final KafkaExporterConfiguration configuration, final Logger logger) {
        this.configuration = configuration;
        this.logger = logger;
        Map<String, Object> kafkaProperties = new HashMap<>();
        //                    of("brokers", bootstrapServers),
        //                    of("sslKeystoreLocation", sslKeystoreLocation),
        //                    of("sslKeystorePassword", sslKeystorePassword),
        //                    of("sslTruststoreLocation", sslTruststoreLocation),
        //                    of("sslTruststorePassword", sslTruststorePassword),
        //                    of("securityProtocol", securityProtocol),
        //                    of("sslKeyPassword", sslKeyPassword),
        //                    of("saslJaasConfig", "RAW(" + decodedSaslJaasConfig + ")"),
        //                    of("saslMechanism", saslMechanism),
        //                    of("serializerClass", KafkaAvroSerializer.class.getName()),
        //                    of("valueDeserializer", KafkaAvroDeserializer.class.getName()),
        //                    of("specificAvroReader", "true"),
        //                    of("schemaRegistryURL", schemaRegistryURL)

        String clientId = buildKafkaClientId(logger);
        kafkaProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka:9092");
        kafkaProperties.put(ProducerConfig.CLIENT_ID_CONFIG, clientId);
        kafkaProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        kafkaProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        this.producer = new KafkaProducer<>(kafkaProperties);

        logger.info("configured Kafka producer with client id {}", clientId);
    }

    public static String buildKafkaClientId(Logger logger) {
        String clientId;
        try {
            clientId = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            logger.error("failed to resolve local hostname, picking random clientId");
            clientId = UUID.randomUUID().toString();
        }
        return clientId;
    }

    public void close() {
        producer.close();
    }

    public void index(final Record<?> record) {
        if (metrics == null) {
            metrics = new KafkaExporterMetrics(record.getPartitionId());
        }

        if (configuration.shouldIndexRecord(record)) {
            logger.debug("sending record to kafka: {}", record.toJson());
            sentToKafka.incrementAndGet();
            metrics.recordBulkSize(1);
            producer.send(new ProducerRecord<>(kafkaTopic, idFor(record), record.toJson()));
        } else {
            logger.debug("skipping record: {}", record.toString());
        }
    }

    /**
     * @return true if all bulk records where flushed successfully
     */
    public boolean flush() {
        if (sentToKafka.get() > 0) {
            producer.flush();
            logger.info("flushed {} exported records to Kafka", sentToKafka.get());
            sentToKafka.set(0);
        }
        return true;
    }

    public boolean shouldFlush() {
        return sentToKafka.get() >= configuration.bulk.size;
    }

    protected String idFor(final Record<?> record) {
        return record.getPartitionId() + "-" + record.getPosition();
    }
}
