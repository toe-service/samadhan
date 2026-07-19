package com.samadhan.enums;

public enum VehicleCategoryEnum {
    SMALL_VEHICLE("Small Vehicle"),
    OPEN_BODY_TRUCK("Open Body Truck"),
    CLOSED_CONTAINER_TRUCK("Closed Container Truck"),
    TRAILER("Trailer");

    private final String displayName;

    VehicleCategoryEnum(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
