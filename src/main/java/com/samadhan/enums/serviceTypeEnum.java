package com.samadhan.enums;

public enum serviceTypeEnum {
	ServiceCentre(1,"vehicleCentre"),
	TOWSERVICE(2,"TowService"),
	TRANSFERSERVICE(3,"TransferService"),
	BOTH(4,"Both");
	
	
	int id;
	String type;
	private serviceTypeEnum(int id, String type) {
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
		for(serviceTypeEnum b:values()) {
			if(b.getType().equalsIgnoreCase(type)) {
				return b.getId();
			}
		}
		return null;
	}
	
	public static String getTypeById(int id) {
		for(serviceTypeEnum b:values()) {
			if(b.getId()==id) {
				return b.getType();
			}
		}
		return null;
	}
	
}
