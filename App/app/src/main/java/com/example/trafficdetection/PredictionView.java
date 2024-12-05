package com.example.trafficdetection;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trafficdetection.dto.PredictionDto;
import com.example.trafficdetection.retrofit.RetrofitAPI;
import com.example.trafficdetection.retrofit.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PredictionView extends AppCompatActivity {

    String TAG = "PredictionView";

    private RecyclerView recyclerView;
    private PredictionAdapter adapter;

    private Context context;  // Context 추가
    private SharedPreferences appData; // SharedPreferences 변수 추가

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prediction_list);

        // Activity의 context를 사용
        context = this;  // 여기서 context를 초기화

        recyclerView = findViewById(R.id.recyclerViewPredictions);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // SharedPreferences 초기화
        appData = context.getSharedPreferences("USER_PREF", Context.MODE_PRIVATE); // SharedPreferences 초기화

        // 저장된 userid 가져오기
        String userid = appData.getString("ID", "Unknown User");
        Log.d(TAG, "Logged in User ID: " + userid);  // 사용자 ID 출력

        // RetrofitClient 인스턴스 가져오기
        RetrofitAPI apiService = RetrofitClient.getInstance().getRetrofitAPI();
        Call<List<PredictionDto>> call = apiService.getPredictions(userid);

        call.enqueue(new Callback<List<PredictionDto>>() {
            @Override
            public void onResponse(Call<List<PredictionDto>> call, Response<List<PredictionDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<PredictionDto> predictions = response.body();
                    // RecyclerView에 데이터 설정
                    adapter = new PredictionAdapter(predictions);
                    recyclerView.setAdapter(adapter);
                } else {
                    Log.e("MainActivity", "Response failed");
                }
            }

            @Override
            public void onFailure(Call<List<PredictionDto>> call, Throwable t) {
                Log.e("MainActivity", "Request failed", t);
            }
        });
    }
}
