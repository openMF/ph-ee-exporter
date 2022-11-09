package hu.dpc.rt.kafkastreamer.exporter;

import io.camunda.zeebe.protocol.record.Record;
import io.camunda.zeebe.protocol.record.RecordType;
import io.camunda.zeebe.protocol.record.ValueType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaExporterConfiguration {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public String kafkaUrl;
    public String kafkaTopic = "zeebe-export";

    public BulkConfiguration bulk = new BulkConfiguration();


    public KafkaExporterConfiguration() {
        kafkaUrl = System.getenv("ZEEBE_KAFKAEXPORT_URL");
        logger.info("DPC Kafka exporter configuration:\nKafka bootstrap URL: {}", kafkaUrl);
    }

    public boolean shouldIndexRecord(final Record<?> record) {
        return shouldIndexRecordType(record.getRecordType()) && shouldIndexValueType(record.getValueType());
    }

    public boolean shouldIndexValueType(final ValueType valueType) {
        switch (valueType) {
            case DEPLOYMENT:
                return true;
            case ERROR:
                return true;
            case INCIDENT:
                return true;
            case JOB:
                return true;
            case JOB_BATCH:
                return false;
            case MESSAGE:
                return false;
            case MESSAGE_SUBSCRIPTION:
                return false;
            case VARIABLE:
                return true;
            case VARIABLE_DOCUMENT:
                return true;
            case PROCESS_INSTANCE:
                return true;
            default:
                return false;
        }
    }

    public boolean shouldIndexRecordType(final RecordType recordType) {
        switch (recordType) {
            case EVENT:
                return true;
            case COMMAND:
                return false;
            case COMMAND_REJECTION:
                return false;
            default:
                return false;
        }
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
}
