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

import io.quarkus.kafka.client.serialization.ObjectMapperSerde;

@ApplicationScoped
public class TopologyProducer {

    static final String LOCATION_RECORDS_STORE = "location-records-store";
    private static final String LOCATION_RECORDS_TOPIC = "location-records";
    private static final String SHIPMENT_RECORDS_TOPIC = "shipment-records";

    @Produces
    public Topology buildTopology() {
        // JSON Serialization / Deserialization
        ObjectMapperSerde<LocationRecord> locationRecordSerde = new ObjectMapperSerde<>(LocationRecord.class);
        ObjectMapperSerde<ShipmentRecord> shipmentRecordSerde = new ObjectMapperSerde<>(ShipmentRecord.class);

        // Inject a key/value store to perform stateful processing
        StreamsBuilder builder = new StreamsBuilder();
        StoreBuilder<KeyValueStore<String,ShipmentRecord>> keyValueStoreBuilder = Stores.keyValueStoreBuilder(Stores.persistentKeyValueStore(LOCATION_RECORDS_STORE), Serdes.String(), shipmentRecordSerde);
        builder.addStateStore(keyValueStoreBuilder);

        // Kafka Stream definition
        builder.stream(LOCATION_RECORDS_TOPIC, Consumed.with(Serdes.String(), locationRecordSerde))
                .process(() -> { return new LocationToShipmentProcessor(); }, LOCATION_RECORDS_STORE)
                .to(SHIPMENT_RECORDS_TOPIC, Produced.with(Serdes.String(), shipmentRecordSerde));

        return builder.build();
    }
}
