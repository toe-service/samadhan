package com.samadhan.repository;

import com.samadhan.entity.AppVersionConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppVersionConfigRepository extends JpaRepository<AppVersionConfig, String> {
}
