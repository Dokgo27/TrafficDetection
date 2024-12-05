package com.example.trafficdetection;

import com.example.trafficdetection.dto.LoginRequest;
import com.example.trafficdetection.retrofit.RetrofitAPI;
import com.example.trafficdetection.retrofit.RetrofitClient;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;

import androidx.appcompat.app.AppCompatActivity;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;


public class LoginActivity extends AppCompatActivity {

    // 컴포넌트 초기화
    Button loginButton;
    Button signupButton;
    EditText idText,pwdText;

    String TAG = "LoginActivity";

    private SharedPreferences appData; //SharedPreferences객체 변수 생성

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login); // 레이아웃 뷰 설정

        // 시스템 바 인셋 설정
        // 시스템 바의 인셋을 처리하여 UI가 시스템 바와 겹치지 않도록 한다.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // EditText와 버튼 초기화
        idText = findViewById(R.id.login_id_textField);
        pwdText = findViewById(R.id.login_password_textField);
        loginButton = findViewById(R.id.btn_login);
        signupButton = findViewById(R.id.btn_register);

        // SharedPreferences 초기화
        appData = getSharedPreferences("USER_PREF", MODE_PRIVATE); // 초기화 추가


        loginButton.setOnClickListener(v -> {
            // EditText에 있는 값 가져오기
            String userid = idText.getText().toString();
            String password = pwdText.getText().toString();

            // 1. ID, PASSWORD 하나라도 입력 안했으면 팝업 생성
            if (userid.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "아이디와 비밀번호를 입력하세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            // 2. loginUser 함수 호출

            loginUser(userid, password);
        });

        signupButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);  // SignupActivity 이동
        });

    }

    // 로그인 요청을 서버에 보냄
    private void loginUser(String userid, String password) {

        // RetrofitClient 인스턴스 가져오기
        RetrofitAPI apiService = RetrofitClient.getInstance().getRetrofitAPI();

        // 로그인 요청 생성
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUserid(userid);  // userid 설정
        loginRequest.setPassword(password);  // password 설정

        Call<ResponseBody> call = apiService.login(loginRequest);

        // 비동기 네트워크 요청
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    // 사용자 ID를 SharedPreferences에 저장
                    // SharedPreferences 객체만으론 저장 불가능 Editor 사용
                    SharedPreferences.Editor editor = appData.edit();
                    editor.putString("ID", userid.trim());
                    // apply, commit 을 안하면 변경된 내용이 저장되지 않음
                    editor.apply();

                    // 로그인 성공 처리 (예: 다음 화면으로 이동)
                    Intent intent = new Intent(LoginActivity.this, DetectActivity.class);
                    startActivity(intent);
                    finish();  // 로그인 후 이전 액티비티 종료
                } else {
                    // 로그인 실패 처리 (예: 오류 메시지 출력)
                    Toast.makeText(LoginActivity.this, "로그인 실패: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                // 네트워크 오류 처리
                Toast.makeText(LoginActivity.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("LoginError", t.getMessage());
            }
        });
    }


}