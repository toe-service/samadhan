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

	// Saves the user's current city (see PATCH /v1/user/{userId}/device-city) — the other half of
	// AvailableRidesNotificationScheduler's matching, alongside updateFcmToken above. No-ops
	// silently if deviceCity couldn't be resolved (caller's reverse-geocode came back empty).
	public void updateDeviceCity(Long userId, String deviceCity);

}
