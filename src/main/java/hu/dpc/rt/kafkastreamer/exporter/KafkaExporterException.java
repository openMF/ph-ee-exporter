package hu.dpc.rt.kafkastreamer.exporter;

public class KafkaExporterException extends RuntimeException {

    public KafkaExporterException(final String message) {
        super(message);
    }

    public KafkaExporterException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
