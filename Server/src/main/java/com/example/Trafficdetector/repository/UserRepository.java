package com.example.Trafficdetector.repository;

import com.example.Trafficdetector.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    User findByUserid(String userid);
}

