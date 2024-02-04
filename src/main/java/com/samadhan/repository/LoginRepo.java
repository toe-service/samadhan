package com.samadhan.repository;

import com.samadhan.entity.Login;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LoginRepo extends CrudRepository<Login, String> {

//    @Override
//    Optional<Login> findById(String s);
}
