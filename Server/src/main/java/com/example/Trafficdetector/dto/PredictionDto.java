package com.example.Trafficdetector.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PredictionDto {

    private Long id; // 새로 추가된 고유 식별자
    private String userid;
    private String prediction;
    private float confidence;
    private String timestamp;

}
