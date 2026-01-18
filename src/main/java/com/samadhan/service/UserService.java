package com.samadhan.service;

import com.samadhan.entity.UserDetails;

public interface UserService {

    public UserDetails findById(Long userId);

}
