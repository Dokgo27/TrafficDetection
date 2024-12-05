package com.example.Trafficdetector.controller;

import com.example.Trafficdetector.dto.PredictionDto;
import com.example.Trafficdetector.dto.UserDto;
import com.example.Trafficdetector.service.PredictionService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/predictions")
@Data
public class PredictionController {

    @Autowired
    private PredictionService predictionService;

    @PostMapping("/send")
    public ResponseEntity<String> sendPrediction(@RequestBody PredictionDto predictionDto) {
        predictionService.savePrediction(predictionDto);
        return ResponseEntity.ok("Prediction received successfully");
    }

    @GetMapping("/{userid}")
    public List<PredictionDto> getPredictionsByUserId(@PathVariable String userid) {
        return predictionService.getPredictionsByUserId(userid);
    }
}
