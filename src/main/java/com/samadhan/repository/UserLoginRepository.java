package com.samadhan.repository;

import com.samadhan.entity.UserLoginEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserLoginRepository extends CrudRepository<UserLoginEntity, Long> {

}
