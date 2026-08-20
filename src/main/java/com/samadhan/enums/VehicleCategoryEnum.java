package com.samadhan.enums;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

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

    /** Constant names and display names, normalised, to the constant they stand for. */
    private static final Map<String, VehicleCategoryEnum> LOOKUP = buildLookup();

    private static Map<String, VehicleCategoryEnum> buildLookup() {
        Map<String, VehicleCategoryEnum> lookup = new HashMap<>();
        for (VehicleCategoryEnum category : values()) {
            lookup.put(normalise(category.name()), category);
        }
        // Constant names win where a display name would normalise onto one of them.
        for (VehicleCategoryEnum category : values()) {
            lookup.putIfAbsent(normalise(category.displayName), category);
        }
        return lookup;
    }

    private static String normalise(String value) {
        return value.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]", "");
    }

    /**
     * Resolves what the client sent, which is either the constant name ("OPEN_BODY_TRUCK")
     * or the display name ("Open Body Truck"). Blank means "no category given".
     */
    public static VehicleCategoryEnum fromValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        VehicleCategoryEnum category = LOOKUP.get(normalise(value));
        if (category == null) {
            throw new IllegalArgumentException("Unknown vehicle category: " + value);
        }
        return category;
    }
}
