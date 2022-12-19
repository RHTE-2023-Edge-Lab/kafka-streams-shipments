package fr.itix.shipments.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
@JsonIgnoreProperties(value = { "complete" })
public class ShipmentRecord {
    public String parcelNumber;
    public String from;
    public String to;
    public long timestamp;

    public ShipmentRecord() {
    }

    public ShipmentRecord(String parcelNumber, String from, String to, long timestamp) {
        this.parcelNumber = parcelNumber;
        this.from = from;
        this.to = to;
        this.timestamp = timestamp;
    }

    public ShipmentRecord updateFrom(LocationRecord location) {
        if (this.timestamp == 0) {
            this.to = location.location;
            this.timestamp = location.timestamp;
            this.parcelNumber = location.parcelNumber;
        } else {
            if (! this.parcelNumber.equals(location.parcelNumber)) {
                throw new IllegalArgumentException("Wrong parcel number");
            }
            if (location.timestamp < this.timestamp) {
                // cannot go back in time
                return null;
            }
            if (this.to.equals(location.location)) {
                // the parcel did not move, discard this event
                return null;
            }
            this.from = this.to;
            this.to = location.location;
            this.timestamp = location.timestamp;
        }

        return this;
    }

    public boolean isComplete() {
        return this.from != null && !this.from.equals("");
    }
}
