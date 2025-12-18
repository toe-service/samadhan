package com.samadhan.enums;

public enum rideStatusEnum {

	ACCEPTED(1,"ACCEPTED"),
	ONGOING(2,"ONGOING"),
	COMPLETED(3,"COMPLETED"),
	CANCELLED(4,"CANCELLED");
	
	
	int id;
	String type;
	
	private rideStatusEnum(int id, String type) {
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
		for(rideStatusEnum b:values()) {
			if(b.getType().equalsIgnoreCase(type)) {
				return b.getId();
			}
		}
		return null;
	}
	
	public static String getTypeById(int id) {
		for(rideStatusEnum b:values()) {
			if(b.getId()==id) {
				return b.getType();
			}
		}
		return null;
	}
	

}
