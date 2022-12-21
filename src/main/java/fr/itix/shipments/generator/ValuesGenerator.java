package fr.itix.shipments.generator;

import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.enterprise.context.ApplicationScoped;

import io.smallrye.mutiny.Multi;
import io.smallrye.reactive.messaging.kafka.Record;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.jboss.logging.Logger;

import fr.itix.shipments.model.LocationRecord;

@ApplicationScoped
public class ValuesGenerator {

    private static final Logger LOG = Logger.getLogger(ValuesGenerator.class);

    private Random random = new Random();

    @ConfigProperty(name = "location-generator.enabled", defaultValue = "false")
    boolean enabled;

    private List<String> warehouses = List.of("PAR", "BRU", "LON", "LIS", "ATH", "STO", "VAR", "DUB", "BUC", "BRN");
    private List<String> directions = List.of("in", "out");
    private List<String> parcelNumbers = List.of("00:00:00:00", "11:11:11:11", "22:22:22:22", "33:33:33:33", "44:44:44:44", "55:55:55:55", "66:66:66:66", "77:77:77:77", "88:88:88:88", "99:99:99:99");

    @Outgoing("location-records")                                        
    public Multi<Record<String, LocationRecord>> generate() {
        LOG.infov("Location record generator is {0}", enabled);
        if (!enabled) {
            return Multi.createFrom().empty();
        }

        return Multi.createFrom().ticks().every(Duration.ofMillis(500))
                .onOverflow().drop()
                .map(tick -> {
                    String warehouse = warehouses.get(random.nextInt(warehouses.size()));
                    String direction = directions.get(random.nextInt(directions.size()));
                    String parcelNumber = parcelNumbers.get(random.nextInt(parcelNumbers.size()));
                    long timestamp = new Date().getTime();

                    return Record.of(parcelNumber, new LocationRecord(parcelNumber, warehouse, direction, timestamp));
                });
    }
}
