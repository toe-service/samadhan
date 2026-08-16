package com.samadhan.enums;

public enum UserRole {
    USER("USER"),
    DRIVER("DRIVER"),
    VEHICLE("VEHICLE"),
    VENDOR("VENDOR"),
    SERVICE_CENTER("SERVICE_CENTER"),
    ADMIN("ADMIN");

    private final String value;

    UserRole(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static UserRole fromValue(String value) {
        for (UserRole role : UserRole.values()) {
            if (role.value.equalsIgnoreCase(value)) {
                return role;
            }
        }
        return USER;
    }
}
