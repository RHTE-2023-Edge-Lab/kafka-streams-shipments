# Kafka Setup

```sh
rm -rf /tmp/kafka-streams
podman-compose up -d
podman exec broker kafka-topics --bootstrap-server broker:9092 --create --topic shipment-records
podman exec broker kafka-topics --bootstrap-server broker:9092 --create --topic location-records
```

**kcat.conf**:

```ini
bootstrap.servers=localhost:9092
security.protocol=PLAINTEXT

# Best practice for higher availability in librdkafka clients prior to 1.7
session.timeout.ms=45000
```

```sh
kcat -b localhost:9092 -L -F kcat.conf
```

```sh
kcat -b localhost:9092 -C -F kcat.conf -t shipment-records
kcat -b localhost:9092 -C -F kcat.conf -t location-records
```
