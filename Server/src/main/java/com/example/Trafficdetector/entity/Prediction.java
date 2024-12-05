package com.example.Trafficdetector.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Prediction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 새로운 ID 필드

    private String userid;
    private String prediction;
    private float confidence;
    private String timestamp;

}
