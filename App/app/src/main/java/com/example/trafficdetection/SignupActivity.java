package com.example.trafficdetection;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.trafficdetection.dto.SignupRequest;
import com.example.trafficdetection.retrofit.RetrofitAPI;
import com.example.trafficdetection.retrofit.RetrofitClient;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignupActivity extends AppCompatActivity {

    // 컴포넌트 초기화
    Button join_submit;
    Button signupButton;
    EditText join_nameText, join_idText,join_pwdText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String TAG = "oncreate";
        Log.d(TAG, "onCreate called");  // onCreate 호출 확인
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup); // 레이아웃 뷰 설정

        // 시스템 바 인셋 설정
        // 시스템 바의 인셋을 처리하여 UI가 시스템 바와 겹치지 않도록 한다.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.signup), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // EditText 초기화
        join_nameText = findViewById(R.id.join_name_textField);
        join_idText = findViewById(R.id.join_id_textField);
        join_pwdText = findViewById(R.id.join_password_textField);

        // btn_join_submit 버튼 초기화
        join_submit = findViewById(R.id.join_submit_button);
        // 리스너
        join_submit.setOnClickListener(v -> {
            String name = join_nameText.getText().toString();
            String userId = join_idText.getText().toString();
            String password = join_pwdText.getText().toString();

            // 회원가입 요청 메서드 호출
            signupUser(name, userId, password);
        });
    }
    // 회원가입 요청을 서버에 보내는 메서드
    private void signupUser(String name, String userId, String password) {
        // Retrofit 인스턴스 가져오기
        RetrofitAPI apiService = RetrofitClient.getInstance().getRetrofitAPI();

        // SignupRequest DTO 생성
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setName(name);
        signupRequest.setUserid(userId);
        signupRequest.setPassword(password);

        // API 호출
        Call<ResponseBody> call = apiService.signup(signupRequest);

        // 비동기 요청
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    // 회원가입 성공 처리
                    Toast.makeText(SignupActivity.this, "회원가입 성공", Toast.LENGTH_SHORT).show();
                    finish();  // 현재 액티비티 종료
                } else {
                    // 회원가입 실패 처리
                    Toast.makeText(SignupActivity.this, "회원가입 실패: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                // 네트워크 오류 처리
                Toast.makeText(SignupActivity.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("SignupError", t.getMessage());
            }
        });
    }

}
