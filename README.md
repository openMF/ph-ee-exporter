## Purpose
This exporter component is in charge of exporting the Zeebe workflow engine's gathered events in JSON form into a Kafka topic. Once built, this component runs insize the Zeebe broker engine.

## Configuration
The following environment variables can be used to configure the exporter:

| Environment variable | Description |
| --- | --- |
| ZEEBE_KAFKAEXPORT_URL | Kafka broker URL |
| ZEEBE_KAFKAEXPORT_TOPIC | Kafka topic to export events to |
| ZEEBE_KAFKAEXPORT_TOPIC_PARTITIONS | Kafka topic partition count (used on auto topic creation) |
| ZEEBE_KAFKAEXPORT_TOPIC_REPLICATION_FACTOR | Kafka topic replication factor (used on auto topic creation) |
| ZEEBE_MSK_ENABLED | Enable MSK (AWS Managed Kafka) support |
```
