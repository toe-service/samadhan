package com.samadhan.response;

public class LoginResponse {
		private String username;
	    private String userType;
	    private Long vehicleId;
	    private Long driverId;
	    private Long vendorId;
	    
	    
		public Long getVendorId() {
			return vendorId;
		}
		public void setVendorId(Long vendorId) {
			this.vendorId = vendorId;
		}
		public String getUsername() {
			return username;
		}
		public void setUsername(String username) {
			this.username = username;
		}
	
		public String getUserType() {
			return userType;
		}
		public void setUserType(String userType) {
			this.userType = userType;
		}
		public Long getVehicleId() {
			return vehicleId;
		}
		public void setVehicleId(Long vehicleId) {
			this.vehicleId = vehicleId;
		}
		public Long getDriverId() {
			return driverId;
		}
		public void setDriverId(Long driverId) {
			this.driverId = driverId;
		}
	    
	    

}
