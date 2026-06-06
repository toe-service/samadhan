package com.samadhan.enums;

public enum VendorStatusEnum {

	VERIFICATION_PENDING(1, "Verification Pending"),
    VERIFIED(2, "Verified"),
    SUBSCRIPTION_PENDING(3, "Subscription Pending"),
    ACTIVE(4, "Active"),
    REJECTED(5, "Rejected"),
    SUSPENDED(6, "Suspended");

    private final int id;
    private final String label;

    VendorStatusEnum(int id, String label) {
        this.id = id;
        this.label = label;
    }

    public int getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public static Integer getIdByType(String type) {
        for (VendorStatusEnum status : values()) {
            if (status.getLabel().equalsIgnoreCase(type)
                    || status.name().equalsIgnoreCase(type)) {
                return status.getId();
            }
        }
        throw new IllegalArgumentException(
                "Invalid vendor status: " + type);
    }

    public static String getTypeById(int id) {
        for (VendorStatusEnum status : values()) {
            if (status.getId() == id) {
                return status.getLabel();
            }
        }
        throw new IllegalArgumentException(
                "Invalid vendor status id: " + id);
    }
}