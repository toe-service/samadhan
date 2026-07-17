package com.samadhan.enums;

public enum serviceTypeEnum {
	TRANSFERSERVICE(0,"TRANSFER_SERVICE"),
	BOOKVEHICLE(1,"BOOK_VEHICLE");
	
	
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
