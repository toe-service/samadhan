package com.samadhan.enums;

public enum VehicleTypeEnum {

	Sedan(0,"Sedan"),
	SUV(1,"SUV"),
	Hatchback(2,"Hatchback"),
	Luxury(3,"Luxury");
	
	
	int id;
	String type;
	private VehicleTypeEnum(int id, String type) {
		this.id = id;
		this.type = type;
	}
	public int getId() {
		return id;
	}
	public String getType() {
		return type;
	}
	
	public static Integer getIdByType(String type) {
		for(VehicleTypeEnum b:values()) {
			if(b.getType().equalsIgnoreCase(type)) {
				return b.getId();
			}
		}
		return null;
	}
	
	public static String getTypeById(int id) {
		for(VehicleTypeEnum b:values()) {
			if(b.getId()==id) {
				return b.getType();
			}
		}
		return null;
	}
	

}


