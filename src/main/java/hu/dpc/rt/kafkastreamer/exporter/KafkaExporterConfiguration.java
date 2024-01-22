package hu.dpc.rt.kafkastreamer.exporter;

import io.camunda.zeebe.protocol.record.Record;
import io.camunda.zeebe.protocol.record.RecordType;
import io.camunda.zeebe.protocol.record.ValueType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ObjectUtils;

import java.util.Optional;

public class KafkaExporterConfiguration {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public String kafkaUrl;
    public String kafkaTopic;
    public Integer kafkaTopicPartitions;
    public Integer kafkaTopicReplicationFactor;

    public BulkConfiguration bulk = new BulkConfiguration();


    public KafkaExporterConfiguration() {
        kafkaUrl = System.getenv("ZEEBE_KAFKAEXPORT_URL");
        kafkaTopic = System.getenv("ZEEBE_KAFKAEXPORT_TOPIC");
        if (ObjectUtils.isEmpty(kafkaTopic)) {
            kafkaTopic = "zeebe-export";
        }
        kafkaTopicPartitions = parseEnvInt("ZEEBE_KAFKAEXPORT_TOPIC_PARTITIONS");
        kafkaTopicReplicationFactor = parseEnvInt("ZEEBE_KAFKAEXPORT_TOPIC_REPLICATION_FACTOR");
        logger.info("Kafka exporter configuration: {}", this);
    }

    private Integer parseEnvInt(String key) {
        try {
            return Integer.parseInt(System.getenv(key));
        } catch (Exception e) {
            logger.warn("Failed to parse env var '{}' with value '{}' as int", key, System.getenv(key));
            return null;
        }
    }

    @Override
    public String toString() {
        return "KafkaExporterConfiguration {" +
                "kafkaUrl='" + kafkaUrl + '\'' +
                ", kafkaTopic='" + kafkaTopic + '\'' +
                ", kafkaTopicPartitions='" + kafkaTopicPartitions + '\'' +
                ", kafkaTopicReplicationFactor='" + kafkaTopicReplicationFactor + '\'' +
                '}';
    }

    public boolean shouldIndexRecord(final Record<?> record) {
        return shouldIndexRecordType(record.getRecordType()) && shouldIndexValueType(record.getValueType());
    }

    public boolean shouldIndexRecordType(final RecordType recordType) {
        return switch (recordType) {
            case COMMAND -> false;
            case COMMAND_REJECTION -> false;
            case EVENT -> true;
            case NULL_VAL -> false;
            case SBE_UNKNOWN -> false;
        };
    }

    public boolean shouldIndexValueType(final ValueType valueType) {
        return switch (valueType) {
            case MESSAGE_BATCH -> false;
            case FORM -> false;
            case CHECKPOINT -> false;
            case COMMAND_DISTRIBUTION -> false;
            case DECISION -> false;
            case DECISION_EVALUATION -> false;
            case DECISION_REQUIREMENTS -> false;
            case DEPLOYMENT -> true;
            case DEPLOYMENT_DISTRIBUTION -> false;
            case ERROR -> true;
            case ESCALATION -> false;
            case INCIDENT -> true;
            case JOB -> true;
            case JOB_BATCH -> false;
            case MESSAGE -> true;
            case MESSAGE_START_EVENT_SUBSCRIPTION -> false;
            case MESSAGE_SUBSCRIPTION -> false;
            case NULL_VAL -> false;
            case PROCESS -> false;
            case PROCESS_EVENT -> true;
            case PROCESS_INSTANCE -> true;
            case PROCESS_INSTANCE_BATCH -> false;
            case PROCESS_INSTANCE_CREATION -> false;
            case PROCESS_INSTANCE_MODIFICATION -> false;
            case PROCESS_INSTANCE_RESULT -> false;
            case PROCESS_MESSAGE_SUBSCRIPTION -> false;
            case RESOURCE_DELETION -> false;
            case SBE_UNKNOWN -> false;
            case SIGNAL -> false;
            case SIGNAL_SUBSCRIPTION -> false;
            case TIMER -> true;
            case VARIABLE -> true;
            case VARIABLE_DOCUMENT -> true;
        };
    }

    public static class BulkConfiguration {
        // delay before forced flush
        public int delay = 5;
        // bulk size before flush
        public int size = 1_000;

        @Override
        public String toString() {
            return "BulkConfiguration{" + "delay=" + delay + ", size=" + size + '}';
        }
    }

    public boolean isMskEnabled() {
        return "true".equalsIgnoreCase(System.getenv("ZEEBE_MSK_ENABLED"));
    }
}
