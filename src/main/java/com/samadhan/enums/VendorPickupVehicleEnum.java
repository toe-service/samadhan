package com.samadhan.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum VendorPickupVehicleEnum {

    // ---------- SMALL VEHICLE ----------
    SCOOTER(
            "Scooter",
            VehicleCategoryEnum.SMALL_VEHICLE,
            20,
            6.0,
            2.0,
            3.0
    ),
    TWO_WHEELER(
            "2 Wheeler",
            VehicleCategoryEnum.SMALL_VEHICLE,
            20,
            6.0,
            2.0,
            3.0
    ),
    E_LOADER(
            "E Loader",
            VehicleCategoryEnum.SMALL_VEHICLE,
            310,
            5.5,
            4.0,
            5.0
    ),
    THREE_WHEELER(
            "3 Wheeler",
            VehicleCategoryEnum.SMALL_VEHICLE,
            500,
            6.5,
            4.5,
            5.5
    ),
    EECO(
            "Eeco",
            VehicleCategoryEnum.SMALL_VEHICLE,
            500,
            6.0,
            4.5,
            4.5
    ),
    TATA_ACE(
            "Tata Ace",
            VehicleCategoryEnum.SMALL_VEHICLE,
            750,
            7.0,
            5.0,
            5.0
    ),

    // ---------- OPEN BODY TRUCK ----------
    PICKUP_8FT(
            "Pickup 8ft",
            VehicleCategoryEnum.OPEN_BODY_TRUCK,
            1250,
            8.0,
            5.5,
            5.5
    ),
    TRUCK_10FT(
            "10ft",
            VehicleCategoryEnum.OPEN_BODY_TRUCK,
            2000,
            10.0,
            6.0,
            6.0
    ),
    TRUCK_14FT_OPEN(
            "14ft Open",
            VehicleCategoryEnum.OPEN_BODY_TRUCK,
            4000,
            14.0,
            6.0,
            6.0
    ),
    TRUCK_15FT_OPEN(
            "15ft Open",
            VehicleCategoryEnum.OPEN_BODY_TRUCK,
            5000,
            15.0,
            6.0,
            6.0
    ),
    TRUCK_17FT_OPEN(
            "17ft Open",
            VehicleCategoryEnum.OPEN_BODY_TRUCK,
            4500,
            17.0,
            6.0,
            6.0
    ),
    TRUCK_19FT_OPEN(
            "19ft Open",
            VehicleCategoryEnum.OPEN_BODY_TRUCK,
            10000,
            19.0,
            7.0,
            7.0
    ),
    TRUCK_20FT_OPEN(
            "20ft Open",
            VehicleCategoryEnum.OPEN_BODY_TRUCK,
            10000,
            20.0,
            7.0,
            8.0
    ),
    TRUCK_22FT_OPEN(
            "22ft Open",
            VehicleCategoryEnum.OPEN_BODY_TRUCK,
            10000,
            22.0,
            7.0,
            8.0
    ),
    TRUCK_22FT_10TYRE(
            "22ft (10 Tyre)",
            VehicleCategoryEnum.OPEN_BODY_TRUCK,
            18000,
            22.0,
            7.5,
            8.0
    ),
    TRUCK_24FT_12TYRE(
            "24ft (12 Tyre)",
            VehicleCategoryEnum.OPEN_BODY_TRUCK,
            25000,
            24.0,
            7.5,
            8.0
    ),
    TRUCK_28FT_14TYRE(
            "28ft (14 Tyre)",
            VehicleCategoryEnum.OPEN_BODY_TRUCK,
            30000,
            28.0,
            8.0,
            8.0
    ),
    TRUCK_30FT_14TYRE(
            "30ft (14 Tyre)",
            VehicleCategoryEnum.OPEN_BODY_TRUCK,
            30000,
            30.0,
            8.0,
            8.0
    ),
    HALF_BODY_TRUCK_32FT(
            "Half Body 32ft",
            VehicleCategoryEnum.OPEN_BODY_TRUCK,
            9000,
            32.0,
            8.0,
            8.0
    ),

    // ---------- CLOSED CONTAINER TRUCK ----------
    TRUCK_14FT(
            "14ft",
            VehicleCategoryEnum.CLOSED_CONTAINER_TRUCK,
            3500,
            14.0,
            6.0,
            6.0
    ),
    TRUCK_14FT_CLOSED(
            "14ft Closed",
            VehicleCategoryEnum.CLOSED_CONTAINER_TRUCK,
            3500,
            14.0,
            6.0,
            6.0
    ),
    TRUCK_17FT(
            "17ft",
            VehicleCategoryEnum.CLOSED_CONTAINER_TRUCK,
            4500,
            17.0,
            6.0,
            6.0
    ),
    TRUCK_17FT_CLOSED(
            "17ft Closed",
            VehicleCategoryEnum.CLOSED_CONTAINER_TRUCK,
            4500,
            17.0,
            6.0,
            6.0
    ),
    TRUCK_19FT(
            "19ft",
            VehicleCategoryEnum.CLOSED_CONTAINER_TRUCK,
            6000,
            19.0,
            7.0,
            7.0
    ),
    CONTAINER_7FT(
            "Container 7ft",
            VehicleCategoryEnum.CLOSED_CONTAINER_TRUCK,
            700,
            7.0,
            5.0,
            5.5
    ),
    CONTAINER_8FT(
            "Container 8ft",
            VehicleCategoryEnum.CLOSED_CONTAINER_TRUCK,
            1000,
            8.0,
            5.5,
            5.5
    ),
    CONTAINER_10FT(
            "Container 10ft",
            VehicleCategoryEnum.CLOSED_CONTAINER_TRUCK,
            2000,
            10.0,
            6.0,
            6.0
    ),
    CONTAINER_20FT(
            "Container 20ft",
            VehicleCategoryEnum.CLOSED_CONTAINER_TRUCK,
            6500,
            20.0,
            7.0,
            7.0
    ),
    CONTAINER_22FT(
            "Container 22ft",
            VehicleCategoryEnum.CLOSED_CONTAINER_TRUCK,
            6500,
            22.0,
            7.0,
            7.0
    ),
    CONTAINER_32FT_9TON(
            "Container 32ft (9 Ton)",
            VehicleCategoryEnum.CLOSED_CONTAINER_TRUCK,
            9000,
            32.0,
            7.5,
            8.0
    ),
    CONTAINER_32FT_18TON(
            "Container 32ft (18 Ton)",
            VehicleCategoryEnum.CLOSED_CONTAINER_TRUCK,
            18000,
            32.0,
            8.0,
            8.0
    ),

    // ---------- TRAILER ----------
    FLATBED_TRAILER_20FT(
            "Flat Bed 20ft",
            VehicleCategoryEnum.TRAILER,
            9000,
            20.0,
            8.0,
            8.0
    ),
    FLATBED_TRAILER_22FT(
            "Flat Bed 22ft",
            VehicleCategoryEnum.TRAILER,
            9000,
            22.0,
            8.0,
            8.0
    ),
    FLATBED_TRAILER_24FT(
            "Flat Bed 24ft",
            VehicleCategoryEnum.TRAILER,
            9000,
            24.0,
            8.0,
            8.0
    ),
    FLATBED_TRAILER_28FT_9TON(
            "Flat Bed 28ft (9 Ton)",
            VehicleCategoryEnum.TRAILER,
            9000,
            28.0,
            8.0,
            8.0
    ),
    FLATBED_TRAILER_28FT_14TON(
            "Flat Bed 28ft (14 Ton)",
            VehicleCategoryEnum.TRAILER,
            14000,
            28.0,
            8.0,
            8.0
    ),
    FLATBED_TRAILER_32FT_9TON(
            "Flat Bed 32ft (9 Ton)",
            VehicleCategoryEnum.TRAILER,
            9000,
            32.0,
            8.0,
            8.0
    ),
    FLATBED_TRAILER_32FT_14TON(
            "Flat Bed 32ft (14 Ton)",
            VehicleCategoryEnum.TRAILER,
            14000,
            32.0,
            8.0,
            8.0
    ),
    FLATBED_TRAILER_40FT_30TON(
            "Flat Bed 40ft (30 Ton)",
            VehicleCategoryEnum.TRAILER,
            30000,
            40.0,
            8.0,
            8.0
    ),
    FLATBED_TRAILER_40FT_32TON(
            "Flat Bed 40ft (32 Ton)",
            VehicleCategoryEnum.TRAILER,
            32000,
            40.0,
            8.0,
            8.0
    ),
    FLATBED_TRAILER_40FT_35TON(
            "Flat Bed 40ft (35 Ton)",
            VehicleCategoryEnum.TRAILER,
            35000,
            40.0,
            8.0,
            8.0
    ),
    FLATBED_TRAILER_40FT_42TON(
            "Flat Bed 40ft (42 Ton)",
            VehicleCategoryEnum.TRAILER,
            42000,
            40.0,
            8.0,
            8.0
    ),
    LOWBED_TRAILER_40FT_30TON(
            "Low Bed 40ft (30 Ton)",
            VehicleCategoryEnum.TRAILER,
            30000,
            40.0,
            8.0,
            8.0
    ),
    LOWBED_TRAILER_40FT_32TON(
            "Low Bed 40ft (32 Ton)",
            VehicleCategoryEnum.TRAILER,
            32000,
            40.0,
            8.0,
            8.0
    ),
    LOWBED_TRAILER_40FT_35TON(
            "Low Bed 40ft (35 Ton)",
            VehicleCategoryEnum.TRAILER,
            35000,
            40.0,
            8.0,
            8.0
    ),
    LOWBED_TRAILER_40FT_42TON(
            "Low Bed 40ft (42 Ton)",
            VehicleCategoryEnum.TRAILER,
            42000,
            40.0,
            8.0,
            8.0
    ),
    SEMIBED_TRAILER_40FT_30TON(
            "Semi Bed 40ft (30 Ton)",
            VehicleCategoryEnum.TRAILER,
            30000,
            40.0,
            8.0,
            8.0
    ),
    SEMIBED_TRAILER_40FT_32TON(
            "Semi Bed 40ft (32 Ton)",
            VehicleCategoryEnum.TRAILER,
            32000,
            40.0,
            8.0,
            8.0
    ),
    SEMIBED_TRAILER_40FT_35TON(
            "Semi Bed 40ft (35 Ton)",
            VehicleCategoryEnum.TRAILER,
            35000,
            40.0,
            8.0,
            8.0
    ),
    SEMIBED_TRAILER_40FT_42TON(
            "Semi Bed 40ft (42 Ton)",
            VehicleCategoryEnum.TRAILER,
            42000,
            40.0,
            8.0,
            8.0
    );

    private final String displayName;
    private final VehicleCategoryEnum category;
    private final Integer maxWeightKg;
    private final Double lengthFt;
    private final Double widthFt;
    private final Double heightFt;

    VendorPickupVehicleEnum(String displayName,
                             VehicleCategoryEnum category,
                             Integer maxWeightKg,
                             Double lengthFt,
                             Double widthFt,
                             Double heightFt) {
        this.displayName = displayName;
        this.category = category;
        this.maxWeightKg = maxWeightKg;
        this.lengthFt = lengthFt;
        this.widthFt = widthFt;
        this.heightFt = heightFt;
    }
    
    public static List<VendorPickupVehicleEnum> getByCategory(
            VehicleCategoryEnum category) {

        return Arrays.stream(VendorPickupVehicleEnum.values())
                .filter(vehicle -> vehicle.getCategory() == category)
                .collect(Collectors.toList());
    }

    public String getDisplayName() {
        return displayName;
    }

    public VehicleCategoryEnum getCategory() {
        return category;
    }

    public Integer getMaxWeightKg() {
        return maxWeightKg;
    }

    public Double getLengthFt() {
        return lengthFt;
    }

    public Double getWidthFt() {
        return widthFt;
    }

    public Double getHeightFt() {
        return heightFt;
    }
}
