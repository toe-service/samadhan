package com.samadhan.enums;

public enum rideStatusEnum {

	PENDING(0,"PENDING"),
	ACCEPTED(1,"ACCEPTED"),
	DECLINED(2,"Declined"),
	READYFORPICKUP(3,"ReadyForPickUp"),
	HANDOVER(4,"HANDOVER"),
	ONGOING(5,"Transferring"),
	YETTOBECOMPLETED(6,"YET TO BE COMPLETED"),
	COMPLETED(7,"COMPLETED"),
	CANCELLED(8,"CANCELLED");
	
	
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
