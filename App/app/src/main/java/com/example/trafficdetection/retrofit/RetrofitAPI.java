package com.example.trafficdetection.retrofit;


import com.example.trafficdetection.dto.LoginRequest;
import com.example.trafficdetection.dto.PredictionDto;
import com.example.trafficdetection.dto.PredictionRequest;
import com.example.trafficdetection.dto.SignupRequest;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface RetrofitAPI {

    @POST("/api/users/login")
    Call<ResponseBody> login(@Body LoginRequest loginRequest);

    @POST("/api/users/signup")
    Call<ResponseBody> signup(@Body SignupRequest signupRequest);

    @POST("/api/predictions/send") // 예측값 전송을 위한 엔드포인트
    Call<ResponseBody> sendPrediction(@Body PredictionRequest predictionRequest); // 예측값 전송 메소드 추가

    @GET("api/predictions/{userid}")
    Call<List<PredictionDto>> getPredictions(@Path("userid") String userid);

}
