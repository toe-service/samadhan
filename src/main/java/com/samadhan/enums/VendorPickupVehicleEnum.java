package com.samadhan.enums;

public enum VendorPickupVehicleEnum {
	
	   SCOOTER(
	            "Scooter",
	            20,
	            6.0,
	            2.0,
	            3.0
	    ),

	    TWO_WHEELER(
	            "2 Wheeler",
	            20,
	            6.0,
	            2.0,
	            3.0
	    ),

	    E_LOADER(
	            "E Loader",
	            310,
	            5.5,
	            4.0,
	            5.0
	    ),

	    THREE_WHEELER(
	            "3 Wheeler",
	            500,
	            6.5,
	            4.5,
	            5.5
	    ),

	    EECO(
	            "Eeco",
	            500,
	            6.0,
	            4.5,
	            4.5
	    ),

	    TATA_ACE(
	            "Tata Ace",
	            750,
	            7.0,
	            5.0,
	            5.0
	    ),

	    PICKUP_8FT(
	            "Pickup 8ft",
	            1250,
	            8.0,
	            5.5,
	            5.5
	    ),

	    TRUCK_10FT(
	            "10ft",
	            1700,
	            10.0,
	            6.0,
	            6.0
	    ),

	    TRUCK_14FT(
	            "14ft",
	            3500,
	            14.0,
	            6.0,
	            6.0
	    ),

	    TRUCK_14FT_OPEN(
	            "14ft Open",
	            3500,
	            14.0,
	            6.0,
	            6.0
	    ),

	    TRUCK_14FT_CLOSED(
	            "14ft Closed",
	            3500,
	            14.0,
	            6.0,
	            6.0
	    ),

	    TRUCK_17FT(
	            "17ft",
	            4500,
	            17.0,
	            6.0,
	            6.0
	    ),

	    TRUCK_17FT_OPEN(
	            "17ft Open",
	            4500,
	            17.0,
	            6.0,
	            6.0
	    ),

	    TRUCK_17FT_CLOSED(
	            "17ft Closed",
	            4500,
	            17.0,
	            6.0,
	            6.0
	    ),

	    TRUCK_19FT(
	            "19ft",
	            6000,
	            19.0,
	            7.0,
	            7.0
	    );

	    private final String displayName;
	    private final Integer maxWeightKg;
	    private final Double lengthFt;
	    private final Double widthFt;
	    private final Double heightFt;

	    VendorPickupVehicleEnum(String displayName,
	                Integer maxWeightKg,
	                Double lengthFt,
	                Double widthFt,
	                Double heightFt) {

	        this.displayName = displayName;
	        this.maxWeightKg = maxWeightKg;
	        this.lengthFt = lengthFt;
	        this.widthFt = widthFt;
	        this.heightFt = heightFt;
	    }

	    public String getDisplayName() {
	        return displayName;
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