package fr.itix.shipments.aggregator;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import fr.itix.shipments.model.LocationRecord;
import fr.itix.shipments.model.ShipmentRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import io.quarkus.kafka.client.serialization.ObjectMapperSerde;

@ApplicationScoped
public class TopologyProducer {
    private static final Logger LOG = Logger.getLogger(TopologyProducer.class);
    static final String KSTREAM_RECORDS_STORE = "kstream-records-store";

    @ConfigProperty(name = "location-records.topic")
    String locationRecordsTopic;

    @ConfigProperty(name = "shipment-records.topic")
    String shipmentRecordsTopic;

    @Produces
    public Topology buildTopology() {
        LOG.infov("Building Kafka Streams topology with input topic {0} and output topic {1}", locationRecordsTopic, shipmentRecordsTopic);

        // JSON Serialization / Deserialization
        ObjectMapperSerde<LocationRecord> locationRecordSerde = new ObjectMapperSerde<>(LocationRecord.class);
        ObjectMapperSerde<ShipmentRecord> shipmentRecordSerde = new ObjectMapperSerde<>(ShipmentRecord.class);

        // Inject a key/value store to perform stateful processing
        StreamsBuilder builder = new StreamsBuilder();
        StoreBuilder<KeyValueStore<String,ShipmentRecord>> keyValueStoreBuilder = Stores.keyValueStoreBuilder(Stores.persistentKeyValueStore(KSTREAM_RECORDS_STORE), Serdes.String(), shipmentRecordSerde);
        builder.addStateStore(keyValueStoreBuilder);

        // Kafka Stream definition
        builder.stream(locationRecordsTopic, Consumed.with(Serdes.String(), locationRecordSerde))
                .process(() -> { return new LocationToShipmentProcessor(); }, KSTREAM_RECORDS_STORE)
                .to(shipmentRecordsTopic, Produced.with(Serdes.String(), shipmentRecordSerde));

        return builder.build();
    }
}
