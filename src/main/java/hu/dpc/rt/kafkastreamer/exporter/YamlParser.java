package hu.dpc.rt.kafkastreamer.exporter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class YamlParser {
    public static Properties parse(String filePath) throws IOException {
        Properties properties = new Properties();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            String currentKey = null;

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty() || line.trim().startsWith("#")) {
                    continue; // Skip empty lines and comments
                }

                if (line.trim().endsWith(":")) {
                    currentKey = line.trim().substring(0, line.indexOf(':'));
                } else if (currentKey != null) {
                    properties.put(currentKey, line.trim());
                    currentKey = null;
                }
            }
        }

        return properties;
    }
}
