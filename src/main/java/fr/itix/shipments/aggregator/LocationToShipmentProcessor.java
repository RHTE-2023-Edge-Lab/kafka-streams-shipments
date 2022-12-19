package fr.itix.shipments.aggregator;

import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;

import fr.itix.shipments.model.LocationRecord;
import fr.itix.shipments.model.ShipmentRecord;

public class LocationToShipmentProcessor implements Processor<String, LocationRecord, String, ShipmentRecord> {
    private KeyValueStore<String, ShipmentRecord> kvStore;
    private ProcessorContext<String, ShipmentRecord> context;

    @Override
    public void process(Record<String, LocationRecord> record) {
        // Retrieve shipment from local store
        ShipmentRecord shipment = kvStore.get(record.key());
        if (shipment == null) {
            shipment = new ShipmentRecord();
        }

        // Update the shipment with the last location and if the shipment has
        // both a source and a destination, forward the record.
        shipment = shipment.updateFrom(record.value());
        if (shipment == null) {
            // This event can be discarded
            return;
        }
        if (shipment.isComplete()) {
            context.forward(record.withKey(shipment.parcelNumber).withValue(shipment));
        }

        // Save the record in the local store
        kvStore.put(record.key(), shipment);
    }

    @Override
    public void init(final ProcessorContext<String, ShipmentRecord> context) {
        this.context = context;
        this.kvStore = (KeyValueStore<String, ShipmentRecord>) context.getStateStore(TopologyProducer.LOCATION_RECORDS_STORE);
    }
}
