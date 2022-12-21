# kafka-streams-shipments Project

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```sh
./mvnw compile quarkus:dev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at http://localhost:8080/q/dev/.

## Configuration

Use the following environment variables to configure this application in production:

| Environment Variable                      | Description                                                                           | Example                             |
|-------------------------------------------|---------------------------------------------------------------------------------------|-------------------------------------|
| `KAFKA_BOOTSTRAP_SERVERS` | coma separated list of Kafka servers (hostname:port) | `localhost:9092` |
| `LOCATION_RECORDS_TOPIC` | Kafka topic where location events are read | `location-records` |
| `SHIPMENT_RECORDS_TOPIC` | Kafka topic where shipment events are produced | `shipment-records` |
| `QUARKUS_KAFKA_STREAMS_TOPICS` | List of all Kafka topics required for this application to start (coma separated list) | `location-records,shipment-records` |
| `QUARKUS_KAFKA_STREAMS_SECURITY_PROTOCOL` | Security protocol used to communicate with the Kafka Broker | `SASL_PLAINTEXT` |
| `QUARKUS_KAFKA_STREAMS_SASL_MECHANISM` | SASL mechanism used to authenticate to the Kafka Broker | `SCRAM-SHA-512` |
| `QUARKUS_KAFKA_STREAMS_SASL_JAAS_CONFIG` | JAAS configuration for Kafka Streams | `org.apache.kafka.common.security.scram.ScramLoginModule required username='myuser' password='s3cr3t';` |
| `MP_MESSAGING_CONNECTOR_SMALLRYE_KAFKA_SECURITY_PROTOCOL` | Same as `QUARKUS_KAFKA_STREAMS_SECURITY_PROTOCOL` | `SASL_PLAINTEXT` |
| `MP_MESSAGING_CONNECTOR_SMALLRYE_KAFKA_SASL_MECHANISM` | Same as `QUARKUS_KAFKA_STREAMS_SASL_MECHANISM` | `SCRAM-SHA-512` |
| `MP_MESSAGING_CONNECTOR_SMALLRYE_KAFKA_SASL_JAAS_CONFIG` | Same as `QUARKUS_KAFKA_STREAMS_SASL_JAAS_CONFIG` | `org.apache.kafka.common.security.scram.ScramLoginModule required username='myuser' password='s3cr3t';` |

## Packaging and running the application

The application can be packaged using:

```sh
./mvnw package
```

It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:

```sh
./mvnw package -Dquarkus.package.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar target/*-runner.jar`.

## Creating a native executable

You can create a native executable using:

```sh
./mvnw package -Pnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using:

```sh
./mvnw package -Pnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/kafka-streams-shipments-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult https://quarkus.io/guides/maven-tooling.

## Container image

```sh
APP_VERSION="$(./mvnw -q -Dexec.executable=echo -Dexec.args='${project.version}' --non-recursive exec:exec)"
podman build -f src/main/docker/Dockerfile.jvm -t quay.io/rhte2023edgelab/kafka-streams-shipments:latest .
podman tag quay.io/rhte2023edgelab/kafka-streams-shipments:latest quay.io/rhte2023edgelab/kafka-streams-shipments:$APP_VERSION
podman push quay.io/rhte2023edgelab/kafka-streams-shipments:$APP_VERSION
podman push quay.io/rhte2023edgelab/kafka-streams-shipments:latest
```

## Development environment

Start the Kafka broker and create two topics.

```sh
podman-compose up -d
podman exec broker kafka-topics --bootstrap-server broker:9092 --create --topic shipment-records
podman exec broker kafka-topics --bootstrap-server broker:9092 --create --topic location-records
```

Create **kcat.conf** with the following content:

```ini
bootstrap.servers=localhost:9092
security.protocol=PLAINTEXT

# Best practice for higher availability in librdkafka clients prior to 1.7
session.timeout.ms=45000
```

Start the following commands in two terminals.

```sh
kcat -b localhost:9092 -C -F kcat.conf -t shipment-records
kcat -b localhost:9092 -C -F kcat.conf -t location-records
```

In case you need to start with a clean state, restart the broker and clean the Kafka Streams storage.

```sh
rm -rf /tmp/kafka-streams
```

## Related Guides

- SmallRye Reactive Messaging - Kafka Connector ([guide](https://quarkus.io/guides/kafka-reactive-getting-started)): Connect to Kafka with Reactive Messaging
