package hu.dpc.rt.kafkastreamer.exporter;

import io.zeebe.exporter.api.Exporter;
import io.zeebe.exporter.api.context.Context;
import io.zeebe.exporter.api.context.Controller;
import io.zeebe.protocol.record.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NoOpExporter implements Exporter {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void configure(Context context) throws Exception {
        logger.info("## no-op exporter configured");
    }

    @Override
    public void export(Record record) {
        // empty
    }

    @Override
    public void open(Controller controller) {
        logger.info("## no-op exporter opened");
    }

    @Override
    public void close() {
        logger.info("## no-op exporter closed");
    }
}
