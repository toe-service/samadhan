package com.samadhan.enums;

public enum CarModelEnum {

    Sedan(0, "Sedan", 1500),
    SUV(1, "SUV", 2000),
    Hatchback(2, "Hatchback", 1200),
    MUV(3, "MUV", 1900),
    PickupTruck(4, "Pickup-Truck", 2500),
    Luxury(5, "Luxury", 1800);

    private final int id;
    private final String label;
    private final int averageWeightKg;

    private CarModelEnum(int id, String label, int averageWeightKg) {
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

    public static Integer getIdByType(String type) {
        for (CarModelEnum v : values()) {
            if (v.getLabel().equalsIgnoreCase(type)) {
                return v.getId();
            }
        }
        throw new IllegalArgumentException("Invalid vehicle type: " + type);
    }

    public static String getTypeById(int id) {
        for (CarModelEnum v : values()) {
            if (v.getId() == id) {
                return v.getLabel();
            }
        }
        throw new IllegalArgumentException("Invalid vehicle id: " + id);
    }
}