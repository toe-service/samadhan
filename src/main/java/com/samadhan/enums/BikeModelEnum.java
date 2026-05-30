package com.samadhan.enums;


public enum BikeModelEnum {

    Commuter(0, "Commuter", 120.0),
    Sports(1, "Sports", 180.0),
    Cruiser(2, "Cruiser", 220.0),
    Adventure(4, "Adventure", 210.0),
    Scooter(5, "Scooter", 105.0),
    Electric(6, "Electric", 140.0),
    SuperBike(3, "Super Bike", 220.0) ;

    private final int id;
    private final String label;
    private final Double averageWeightKg;

    BikeModelEnum(int id, String label, Double averageWeightKg) {
        this.id = id;
        this.label = label;
        this.averageWeightKg = averageWeightKg;
    }

    public int getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public Double getAverageWeightKg() {
        return averageWeightKg;
    }

    public static BikeModelEnum fromId(int id) {
        for (BikeModelEnum b : values()) {
            if (b.id == id) return b;
        }
        throw new IllegalArgumentException("Invalid bike id: " + id);
    }

    public static BikeModelEnum fromType(String type) {
        for (BikeModelEnum b : values()) {
            if (b.label.equalsIgnoreCase(type)) return b;
        }
        throw new IllegalArgumentException("Invalid bike type: " + type);
    }
}
