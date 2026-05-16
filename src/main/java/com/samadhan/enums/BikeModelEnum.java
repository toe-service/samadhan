package com.samadhan.enums;


public enum BikeModelEnum {

    Commuter(0, "Commuter", 120),
    Sports(1, "Sports", 180),
    Cruiser(2, "Cruiser", 220),
    Touring(3, "Touring", 280),
    Adventure(4, "Adventure", 210),
    Scooter(5, "Scooter", 105),
    Electric(6, "Electric", 140);

    private final int id;
    private final String label;
    private final int averageWeightKg;

    BikeModelEnum(int id, String label, int averageWeightKg) {
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

    public int getAverageWeightKg() {
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
