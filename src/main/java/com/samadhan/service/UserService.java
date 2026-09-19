package com.samadhan.service;

import com.samadhan.entity.UserDetails;

public interface UserService {

    public UserDetails findById(Long userId);

    public java.util.Optional<UserDetails> findByUserContactNumber(String userContactNumber);

	public UserDetails loginUser(String userName, String password);

	public UserDetails deactivateUser(Long userId);

	// Saves the device's push token at login (see /v1/user-otp-verify) for the "Available Rides"
	// daily notification. No-ops silently if fcmToken is null (caller didn't send one).
	public void updateFcmToken(Long userId, String fcmToken);

}
