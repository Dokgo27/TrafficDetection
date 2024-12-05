package com.example.trafficdetection.retrofit;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static RetrofitClient instance = null;
    private static RetrofitAPI retrofitAPI;

    private RetrofitClient() {
        //retrofit 설정
        //사용하고 있는 서버 BASE 주소 ex : http://220.69.208.114
        String baseUrl = "http://220.69.208.114:8090/";
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        retrofitAPI = retrofit.create(RetrofitAPI.class);
    }

    // Singleton 패턴으로 인스턴스 반환
    public static RetrofitClient getInstance() {
        if (instance == null) {
            instance = new RetrofitClient();
        }
        return instance;
    }

    // 인스턴스 메서드로 변경
    public RetrofitAPI getRetrofitAPI() {
        return retrofitAPI;
    }
}
