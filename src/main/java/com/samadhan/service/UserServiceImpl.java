package com.samadhan.service;

import com.samadhan.entity.UserDetails;
import com.samadhan.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService{

    @Autowired
    UserRepository userrepo;

    @Override
    public UserDetails findById(Long userId) {
       Optional<UserDetails> userList=userrepo.findById(userId);
        return userList.get();

    }
}
