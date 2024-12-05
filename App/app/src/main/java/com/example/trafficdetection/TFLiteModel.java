package com.example.trafficdetection;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

import com.example.trafficdetection.dto.PredictionRequest;
import com.example.trafficdetection.retrofit.RetrofitAPI;
import com.example.trafficdetection.retrofit.RetrofitClient;

import org.tensorflow.lite.Delegate;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.flex.FlexDelegate;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TFLiteModel {

    private static final String TAG = "TFLiteModel";
    private Interpreter tflite;

    private Context context;  // Context 추가
    private SharedPreferences appData; // SharedPreferences 변수 추가

    private static final int SEQUENCE_LENGTH = 25;
    private static final int IMAGE_HEIGHT = 64;
    private static final int IMAGE_WIDTH = 64;

    private String lastPrediction = ""; // 마지막 예측 결과
    private float lastConfidence = 0.0f; // 마지막 예측 신뢰도

    // TFLite 모델 로딩
    public TFLiteModel(Context context, AssetManager assetManager, String modelPath) throws IOException {
        Log.d(TAG, "Loading TFLite model from: " + modelPath); // 모델 로드 시작 로그

        // SharedPreferences 초기화
        appData = context.getSharedPreferences("USER_PREF", Context.MODE_PRIVATE); // SharedPreferences 초기화

        try {
            // Flex Delegate 생성
            Delegate flexDelegate = new FlexDelegate();
            tflite = new Interpreter(loadModelFile(assetManager, modelPath), new Interpreter.Options().addDelegate(flexDelegate));
            Log.d(TAG, "TFLite model loaded successfully"); // 모델 로드 성공 로그
        } catch (Exception e) {
            Log.e(TAG, "Error loading TFLite model", e); // 모델 로드 실패 로그
            throw new IOException("Failed to load model", e);
        }
    }

    // 모델 파일을 ByteBuffer로 읽기
    private MappedByteBuffer loadModelFile(AssetManager assetManager, String modelPath) throws IOException {
        Log.d(TAG, "Loading model file into ByteBuffer"); // 파일 로드 로그
        AssetFileDescriptor fileDescriptor = assetManager.openFd(modelPath);
        try (FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor())) {
            FileChannel fileChannel = inputStream.getChannel();
            long startOffset = fileDescriptor.getStartOffset();
            long declaredLength = fileDescriptor.getDeclaredLength();
            MappedByteBuffer modelBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
            Log.d(TAG, "Model file loaded into ByteBuffer"); // 파일 로드 성공 로그
            return modelBuffer;
        }
    }


    // 예측 함수 (입력과 출력 설정)
    public String predict(List<TensorImage> frames) {
        Log.d(TAG, "Starting prediction with frames: " + frames.size());

        // 저장된 userid 가져오기
        String userid = appData.getString("ID", "Unknown User");
        Log.d(TAG, "Logged in User ID: " + userid);  // 사용자 ID 출력

        // 입력 배열 준비 (배치 크기, 시퀀스 길이, 높이, 너비, 채널)
        float[][][][][] input = new float[1][SEQUENCE_LENGTH][IMAGE_HEIGHT][IMAGE_WIDTH][3];

        // 각 프레임을 처리하여 입력 배열에 복사
        for (int i = 0; i < SEQUENCE_LENGTH; i++) {
            if (i < frames.size()) { // 프레임 수가 시퀀스 길이보다 작을 경우
                TensorImage image = frames.get(i);
                // 프레임을 64x64 크기로 리사이즈하고 정규화
                TensorImage resizedImage = new ImageProcessor.Builder()
                        .add(new ResizeOp(IMAGE_HEIGHT, IMAGE_WIDTH, ResizeOp.ResizeMethod.BILINEAR))
                        .add(new NormalizeOp(0.0f, 255.0f)) // 정규화
                        .build()
                        .process(image);

                // 데이터 복사
                for (int h = 0; h < IMAGE_HEIGHT; h++) {
                    for (int w = 0; w < IMAGE_WIDTH; w++) {
                        for (int c = 0; c < 3; c++) {
                            input[0][i][h][w][c] = resizedImage.getBuffer().get(h * IMAGE_WIDTH * 3 + w * 3 + c);
                        }
                    }
                }
            }
        }

        // TFLite 모델 예측
        float[][] output = new float[1][3]; // 출력 크기 조정
        try {
            tflite.run(input, output); // 추론 실행
            Log.d(TAG, "Prediction successful: " + output[0][0] + ", " + output[0][1] + ", " + output[0][2]);
        } catch (Exception e) {
            Log.e(TAG, "Error during prediction", e);
        }

        // 가장 높은 확률을 가진 클래스 인덱스 찾기
        int predictedLabel = getPredictedLabel(output);
        float maxConfidence = output[0][predictedLabel]; // 가장 높은 신뢰도

        // 90% 이상일 때만 예측 결과를 업데이트
        if (maxConfidence > 0.95) {
            String[] categories = {"신호위반", "중앙선침범", "진로변경위반"};
            String currentPrediction = categories[predictedLabel];

            // 예측 결과가 변경되었을 경우에만 업데이트
            if (!currentPrediction.equals(lastPrediction)) {
                lastPrediction = currentPrediction;
                lastConfidence = maxConfidence;

                // 예측 시간 가져오기
                String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

                sendPredictionToServer(userid, lastPrediction, lastConfidence, timestamp);
                return lastPrediction; // 새로운 예측 결과 반환
            }
        }

        // 아무것도 아닐 때의 조건을 추가
        if (maxConfidence < 0.95) { // 예시로 50% 미만일 때 '아무것도 아님'으로 처리
            return "아무것도 아님"; // '아무것도 아님' 반환
        }

        return lastPrediction; // 이전 예측 결과 유지
    }

    // 서버에 예측 결과 전송
    public void sendPredictionToServer(String userid, String prediction, float confidence, String timestamp) {

        RetrofitAPI apiService = RetrofitClient.getInstance().getRetrofitAPI();

        // 가져온 userid를 사용
        PredictionRequest predictionRequest = new PredictionRequest();
        predictionRequest.setUserid(userid);
        predictionRequest.setPrediction(prediction);
        predictionRequest.setConfidence(confidence);
        predictionRequest.setTimestamp(timestamp);

        Call<ResponseBody> call = apiService.sendPrediction(predictionRequest);

        // 비동기 네트워크 요청
        call.enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Prediction sent successfully");
                } else {
                    Log.e(TAG, "Failed to send prediction: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "Error sending prediction", t);
            }
        });
    }


    // 예측 결과에서 최대 확률을 가진 클래스 인덱스를 반환
    private int getPredictedLabel(float[][] output) {
        int predictedLabel = 0;
        float maxProbability = output[0][0];
        for (int i = 1; i < output[0].length; i++) {
            if (output[0][i] > maxProbability) {
                maxProbability = output[0][i];
                predictedLabel = i;
            }
        }
        return predictedLabel;
    }
}