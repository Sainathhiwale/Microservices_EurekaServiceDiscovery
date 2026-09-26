package com.examen.userservice.repository;

import com.examen.userservice.domain.UserMaster;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserMasterRepo extends JpaRepository<UserMaster,Long> {
}