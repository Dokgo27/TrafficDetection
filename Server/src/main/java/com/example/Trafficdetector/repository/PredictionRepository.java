package com.example.Trafficdetector.repository;

import com.example.Trafficdetector.entity.Prediction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PredictionRepository extends JpaRepository<Prediction, Long> {
    List<Prediction> findByUserid(String userid);
}
