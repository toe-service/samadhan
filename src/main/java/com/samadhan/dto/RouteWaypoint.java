package com.samadhan.dto;

// A vendor-specified intermediate stop on an availability posting's route (e.g. "via Kanpur"),
// geocoded the same way as the posting's from/to locations. Stored as JSON on
// VendorAvailability — see that entity's getWaypoints/setWaypoints.
public class RouteWaypoint {

	private String location;
	private String latitude;
	private String longitude;

	public RouteWaypoint() {
	}

	public RouteWaypoint(String location, String latitude, String longitude) {
		this.location = location;
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getLatitude() {
		return latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}
}
