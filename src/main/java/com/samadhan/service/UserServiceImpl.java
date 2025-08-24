package com.samadhan.service;

import com.samadhan.entity.User;
import com.samadhan.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService{

    @Autowired
    UserRepository userrepo;

    @Override
    public User findById(Long userId) {
       Optional<User> userList=userrepo.findById(userId);
        return userList.get();

    }
}
