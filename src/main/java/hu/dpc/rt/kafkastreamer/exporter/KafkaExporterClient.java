package hu.dpc.rt.kafkastreamer.exporter;

import io.camunda.zeebe.protocol.record.Record;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class KafkaExporterClient {
    private Logger logger;
    private KafkaExporterMetrics metrics;
    private AtomicLong sentToKafka = new AtomicLong(0);
    private AtomicBoolean kafkaSendingError  = new AtomicBoolean(false);
    private boolean initialized;

    private KafkaProducer<String, String> producer;
    private static KafkaExporterConfiguration configuration = new KafkaExporterConfiguration();

    public KafkaExporterClient(KafkaExporterConfiguration configuration, Logger logger) {
        this.logger = logger;
        String clientId = buildKafkaClientId(logger);

        Map<String, Object> properties = new HashMap<>();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, configuration.kafkaUrl);
        properties.put(ProducerConfig.CLIENT_ID_CONFIG, clientId);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.RETRIES_CONFIG, 3);

        if (configuration.isMskEnabled()) {
            logger.info("configuring Kafka client with AWS MSK support");
            properties.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL");
            properties.put(SaslConfigs.SASL_MECHANISM, "AWS_MSK_IAM");
            properties.put(SaslConfigs.SASL_JAAS_CONFIG, "software.amazon.msk.auth.iam.IAMLoginModule required;");
            properties.put(SaslConfigs.SASL_CLIENT_CALLBACK_HANDLER_CLASS, "software.amazon.msk.auth.iam.IAMClientCallbackHandler");
        } else {
            logger.info("configuring Kafka client for plain Kafka without MSK support)");
        }

        this.producer = new KafkaProducer<>(properties);

        try {
            AdminClient adminClient = AdminClient.create(properties);
            adminClient.createTopics(Arrays.asList(new NewTopic(configuration.kafkaTopic, Optional.ofNullable(configuration.kafkaTopicPartitions), Optional.ofNullable(configuration.kafkaTopicReplicationFactor).map(Integer::shortValue))));
            adminClient.close();
            logger.info("created kafka topic {} with partitions {} and replication factor {} successfully", configuration.kafkaTopic, configuration.kafkaTopicPartitions, configuration.kafkaTopicReplicationFactor);
        } catch (Exception e) {
            logger.warn("Failed to create Kafka topic (it exists already?)", e);
        }

        logger.info("configured Kafka producer with client id {}", clientId);
    }

    public static String buildKafkaClientId(Logger logger) {
        return UUID.randomUUID().toString();
    }

    public void close() {
        producer.close();
    }

    public void index(Record<?> record) {
        if (metrics == null && !initialized) {
            try {
                metrics = new KafkaExporterMetrics(record.getPartitionId());
            } catch (Exception e) {
                logger.warn("## failed to initialize metrics, continuing without it");
            }
            initialized = true;
        }

        if (configuration.shouldIndexRecord(record)) {
            logger.trace("sending record to kafka: {}", record.toJson());
            sentToKafka.incrementAndGet();
            metrics.recordBulkSize(1);
            String key = Long.toString(record.getKey());
            producer.send(new ProducerRecord<>(configuration.kafkaTopic, key, record.toJson()), (recordMetadata, e) -> {
                if (e != null) {
                    logger.warn("sending record to kafka failed: {}", record);
                    kafkaSendingError.set(true);
                }
            });
        } else {
            logger.trace("skipping record: {}", record.toString());
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
        if (kafkaSendingError.get()) {
            logger.warn("flushing revealed an error, not acknowledging any records in the batch");
            kafkaSendingError.set(false);
            return false;
        } else {
            logger.trace("flushing succeeded, acknowledging the last record");
            kafkaSendingError.set(false);
            return true;
        }
    }

    public boolean shouldFlush() {
        return sentToKafka.get() >= configuration.bulk.size;
    }

}
