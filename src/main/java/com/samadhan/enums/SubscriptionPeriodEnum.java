package com.samadhan.enums;

public enum SubscriptionPeriodEnum {
	
    Free(0,"Free"),
	QUARTER(1,"3"),
	HALFYEAR(2,"6"),
	YEARLY(3,"12");
	
	
	int id;
	String type;
	private SubscriptionPeriodEnum(int id, String type) {
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
		for(SubscriptionPeriodEnum b:values()) {
			if(b.getType().equalsIgnoreCase(type)) {
				return b.getId();
			}
		}
		return null;
	}
	
	public static String getTypeById(int id) {
		for(SubscriptionPeriodEnum b:values()) {
			if(b.getId()==id) {
				return b.getType();
			}
		}
		return null;
	}
	

	

	

}
