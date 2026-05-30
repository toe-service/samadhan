package com.samadhan.enums;

public enum CarModelEnum {

    Sedan(0, "Sedan", 1500.0),
    SUV(1, "SUV", 1800.0),
    Hatchback(2, "Hatchback", 1000.0),
    MUV(3, "MUV", 1900.0),
    EV(7, "Electric Car", 1900.0),
    PickupTruck(4, "Pickup-Truck", 2500.0),
    Luxury(5, "Luxury", 2000.0);

    private final int id;
    private final String label;
    private final Double averageWeightKg;

    private CarModelEnum(int id, String label, Double averageWeightKg) {
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