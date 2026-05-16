package com.samadhan.enums;

public enum ParcelTypeEnum {
	
	Car(0,"Car"),
	Bike(1,"Bike"),
	Package(2,"Package");
	
	
	int id;
	String type;
	private ParcelTypeEnum(int id, String type) {
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
		for(ParcelTypeEnum b:values()) {
			if(b.getType().equalsIgnoreCase(type)) {
				return b.getId();
			}
		}
		return null;
	}
	
	public static String getTypeById(int id) {
		for(ParcelTypeEnum b:values()) {
			if(b.getId()==id) {
				return b.getType();
			}
		}
		return null;
	}
	

}
