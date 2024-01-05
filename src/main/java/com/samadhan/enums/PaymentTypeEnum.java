package com.samadhan.enums;

public enum PaymentTypeEnum {


	Free(0,"Free"),
	SILVER(1,"SILVER"),
	GOLD(2,"GOLD"),
	PLATINUM(3,"PLATINUM");
	
	
	int id;
	String type;
	private PaymentTypeEnum(int id, String type) {
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
		for(PaymentTypeEnum b:values()) {
			if(b.getType().equalsIgnoreCase(type)) {
				return b.getId();
			}
		}
		return null;
	}
	
	public static String getTypeById(int id) {
		for(PaymentTypeEnum b:values()) {
			if(b.getId()==id) {
				return b.getType();
			}
		}
		return null;
	}
	

	
}
