package com.example.Trafficdetector.service;

import com.example.Trafficdetector.dto.PredictionDto;
import com.example.Trafficdetector.entity.Prediction;
import com.example.Trafficdetector.repository.PredictionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PredictionService {

    @Autowired
    private PredictionRepository predictionRepository;

    public void savePrediction(PredictionDto predictionDto) {
        Prediction prediction = new Prediction();
        prediction.setUserid(predictionDto.getUserid());
        prediction.setPrediction(predictionDto.getPrediction());
        prediction.setConfidence(predictionDto.getConfidence());
        prediction.setTimestamp(predictionDto.getTimestamp());

        predictionRepository.save(prediction);
    }

    public List<PredictionDto> getPredictionsByUserId(String userid) {
        List<Prediction> predictions = predictionRepository.findByUserid(userid);
        return predictions.stream()
                .map(prediction -> new PredictionDto(prediction.getId(), prediction.getUserid(),
                        prediction.getPrediction(), prediction.getConfidence(),
                        prediction.getTimestamp()))
                .collect(Collectors.toList());
    }
}
