package com.samadhan.service;

import com.samadhan.entity.UserDetails;

public interface UserService {

    public UserDetails findById(Long userId);

    public java.util.Optional<UserDetails> findByUserContactNumber(String userContactNumber);

	public UserDetails loginUser(String userName, String password);

	public UserDetails deactivateUser(Long userId);

}
