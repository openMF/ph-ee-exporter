package hu.dpc.rt.kafkastreamer.exporter;

import java.util.HashMap;
import java.util.Map;

public class Properties {
    private Map<String, Object> properties = new HashMap<>();

    public Object get(String key) {
        return properties.get(key);
    }

    public void put(String key, Object value) {
        properties.put(key, value);
    }
}
