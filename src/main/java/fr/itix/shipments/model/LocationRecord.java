package fr.itix.shipments.model;

public class LocationRecord {

    public String parcelNumber;
    public String location;
    public String direction;
    public long timestamp;

    public LocationRecord() {
    }

    public LocationRecord(String parcelNumber, String location, String direction, long timestamp) {
        this.parcelNumber = parcelNumber;
        this.location = location;
        this.direction = direction;
        this.timestamp = timestamp;
    }
}
