package com.example.trafficdetection;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.graphics.Matrix;

import androidx.activity.EdgeToEdge;
import androidx.camera.core.Camera;

import androidx.core.view.WindowCompat;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.content.pm.PackageManager;
import android.view.View;
import android.widget.Button;

import androidx.camera.core.AspectRatio;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;

import com.google.common.util.concurrent.ListenableFuture;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class DetectActivity extends AppCompatActivity {

    @SuppressLint("RestrictedApi")
    Camera camera =null;

    // 카메라 미리보기를 위한 뷰와 버튼 정의
    PreviewView previewView;
    Button detectionButton;
    // 탐지한 결과를 보여주는 텍스트 뷰
    TextView predictionText;

    Button prediction_list_Button;

    // 카메라 변수 설정
    ProcessCameraProvider processCameraProvider;
    int lensFacing = CameraSelector.LENS_FACING_BACK; // 후면 카메라 사용
    private static final int SEQUENCE_LENGTH = 25; // SEQUENCE_LENGTH 정의

    // 필요한 클래스 설정
    private TFLiteModel tfliteModel;
    private TextView predictionResultView;

    // 필요한 변수 설정
    private boolean buttonState;
    private static final int IMAGE_HEIGHT = 64;
    private static final int IMAGE_WIDTH = 64;

    // 클래스 변수로 frameList 선언
    private List<TensorImage> frameList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String TAG = "oncreate";
        Log.d(TAG, "onCreate called");  // onCreate 호출 확인
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detect); // 레이아웃 뷰 설정

        // 시스템 바 인셋 설정
        // 시스템 바의 인셋을 처리하여 UI가 시스템 바와 겹치지 않도록 한다.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.detect), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 뷰의 컴포넌트와 변수 연결
        previewView = findViewById(R.id.previewView);
        detectionButton = findViewById(R.id.DetectionButton);
        predictionText = findViewById(R.id.predictionText);
        prediction_list_Button = findViewById(R.id.Prediction_list_Button);

        // 버튼 설정
        buttonState = false;

        // 카메라 권한 요청
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, 1);


        //-------------------------------- 버튼 리스너 초기화 --------------------------------//
        // 탐지 버튼 리스너
        detectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(DetectActivity.this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    if (!buttonState) {  // buttonState가 false일 때 (시작)
                        // 미리보기 시작
                        bindPreview();
                        detectionButton.setText("멈춤"); // 버튼 텍스트 변경
                        buttonState = true;  // 상태 변경
                    } else {  // buttonState가 true일 때 (정지)
                        if (processCameraProvider != null) {
                            processCameraProvider.unbindAll(); // 미리보기 정지
                        }
                        detectionButton.setText("시작"); // 버튼 텍스트 다시 "Start"로 변경
                        buttonState = false;  // 상태 변경
                    }
                }
            }
        });

        prediction_list_Button.setOnClickListener(v -> {
            Intent intent = new Intent(DetectActivity.this, PredictionView.class);
            startActivity(intent);  // SignupActivity 이동
        });

        //-------------------------------- tfLite 모델 초기화 --------------------------------//
        try {
            tfliteModel = new TFLiteModel(this, getAssets(), "saved_cnn_model.tflite");
        } catch (IOException e) {
            Log.e(TAG, "Failed to load TFLite model", e);
        }

        // 레이아웃이 시스템 창과의 관계를 처리하는 방식을 제어
        // false로 설정하면 앱의 콘텐츠가 시스템 창과 겹칠 수 있도록 하여 Edge-to-Edge 화면을 구현할 수 있다.
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

    }

    //-------------------------------- 카메라 프로바이더 초기화 --------------------------------//
    private void initializeCamera() {
        String TAG = "initializeCamera";
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                processCameraProvider = cameraProviderFuture.get(); // 카메라 프로바이더 가져오기
                bindPreview(); // 카메라 미리보기 바인딩
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Camera provider initialization failed", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }
    //-------------------------------- 카메라 미리보기와 이미지 분석 바인딩 --------------------------------//
    private void bindPreview() {
        String TAG = "bindPreview";
        Log.d(TAG, "Binding camera preview");

        // 카메라 선택자 설정
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(lensFacing)
                .build();

        // Preview 객체 생성
        Preview preview = new Preview.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .build();

        // ImageAnalysis 객체 생성
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build();

        // 카메라 프로바이더에서 모든 바인딩 해제
        processCameraProvider.unbindAll();
        Log.d(TAG, "ImageAnalysis created"); // ImageAnalysis 생성 로그

        // ObjectDetector와 분석기 설정
        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), new ImageAnalysis.Analyzer() {
            @Override
            public void analyze(@NonNull ImageProxy imageProxy) {
                String TAG = "analyze";
//                Log.d(TAG, "Analyzing frame"); // 프레임 분석 시작 로그
                // 프레임 분석 및 예측 값 도출
                processFrame(imageProxy);
            }
        });

        // Preview 바인딩
        //camera = processCameraProvider.bindToLifecycle(this, cameraSelector, preview);

        // SurfaceProvider 설정

        // ImageAnalysis 바인딩
        processCameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);
        preview.setSurfaceProvider(previewView.getSurfaceProvider());
    }



    //-------------------------------- 프레임 처리(전처리) --------------------------------//
    private void processFrame(ImageProxy imageProxy) {

        // YUV 형식으로 오기 때문에 YuvToRgbConverter로 RGB 형식으로 바꾼 후에 바로 bitmap으로 생성.
        Bitmap bitmap = convertImageProxyToBitmap(imageProxy);

        // bitmap이 null인지 확인
        String TAG1 = "processFrame";
        if (bitmap == null) {
            Log.e(TAG1, "Bitmap is null, skipping frame processing");
            return;  // null인 경우 프레임 처리 중단
        }

        // Bitmap을 TensorImage로 변환
        TensorImage tensorImage = new TensorImage(DataType.UINT8);
        tensorImage.load(bitmap); // Bitmap 로드

        // 리사이즈 및 정규화를 위한 ImageProcessor 설정
        ImageProcessor imageProcessor = new ImageProcessor.Builder()
                .add(new ResizeOp(IMAGE_WIDTH, IMAGE_HEIGHT, ResizeOp.ResizeMethod.BILINEAR)) // 원하는 크기로 리사이즈
                .add(new NormalizeOp(0.0f, 255.0f)) // 정규화 (예: 0-255 범위를 0-1로)
                .build();

        // 이미지 전처리
        TensorImage processedImage = imageProcessor.process(tensorImage);

        // 프레임을 TFLite 입력 형식으로 변환
        frameList.add(processedImage); // processedImage를 사용하여 프레임 리스트에 추가

        String TAG2 = "prediction";
        // 프레임 리스트의 크기가 SEQUENCE_LENGTH(25)에 도달했는지 확인
        if (frameList.size() == SEQUENCE_LENGTH) {

            // TFLiteModel을 사용하여 예측 수행
            // predict() 인수 안에는
            String result = tfliteModel.predict(frameList);

            // 예측 결과 로그 출력
            Log.d(TAG2, "Prediction result: " + result);

            runOnUiThread(() -> {
                // 예측 결과 UI 업데이트
                predictionText.setText(result);
            });
            // 프레임 리스트 초기화
            frameList.clear();
        }
        imageProxy.close();

    }

    // ImageProxy 에서 바이트 배열을 가져와 Bitmap 변환
    // 코드에서 ImageProxy의 프레임이 YUV 형식으로 제공
    // 이 YUV 데이터를 RGB 형식으로 변환한 후 Bitmap으로 변환하는 과정
    private Bitmap convertImageProxyToBitmap(ImageProxy image) {
        String TAG = "convertImageProxyToBitmap";
        ImageProxy.PlaneProxy[] planes = image.getPlanes();

        // YUV를 RGBA로 변환
        int width = image.getWidth();
        int height = image.getHeight();
        ByteBuffer yBuffer = planes[0].getBuffer(); // Y plane
        byte[] yData = new byte[yBuffer.remaining()];
        yBuffer.get(yData);

        // U와 V Plane 존재 여부 확인
        byte[] uData = null;
        byte[] vData = null;
        if (planes.length > 1) {
            ByteBuffer uBuffer = planes[1].getBuffer(); // U plane
            uData = new byte[uBuffer.remaining()];
            uBuffer.get(uData);
        }
        if (planes.length > 2) {
            ByteBuffer vBuffer = planes[2].getBuffer(); // V plane
            vData = new byte[vBuffer.remaining()];
            vBuffer.get(vData);
        }

        // YUV -> RGB 변환 로직 추가 필요
        int[] rgb = new int[width * height];

        // YUV를 RGB로 변환하는 코드
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int Y = yData[i * width + j] & 0xFF; // Y 값
                int U = (uData != null && j % 2 == 0 && i % 2 == 0) ? uData[(i / 2) * (width / 2) + (j / 2)] & 0xFF : 0; // U 값
                int V = (vData != null && j % 2 == 0 && i % 2 == 0) ? vData[(i / 2) * (width / 2) + (j / 2)] & 0xFF : 0; // V 값

                int R = Y + (int) (1.402 * (V - 128));
                int G = Y - (int) (0.344136 * (U - 128) + 0.714136 * (V - 128));
                int B = Y + (int) (1.772 * (U - 128));

                // RGB 값의 범위를 0-255로 제한
                R = Math.min(255, Math.max(0, R));
                G = Math.min(255, Math.max(0, G));
                B = Math.min(255, Math.max(0, B));

                rgb[i * width + j] = (0xFF << 24) | (R << 16) | (G << 8) | B; // ARGB 형식으로 설정
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(rgb, 0, width, 0, 0, width, height);

        // 이미지 회전 처리
        int rotationDegrees = image.getImageInfo().getRotationDegrees();
        if (rotationDegrees != 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(rotationDegrees);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        }

        return bitmap;
    }


    //-------------------------------- 사용자 권한 요청 결과를 처리 --------------------------------//
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        String TAG = "onRequestPermissionsResult";
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 권한이 허용되면 카메라를 초기화
                initializeCamera();
            } else {
                Log.e(TAG, "Camera permission not granted");
            }
        }
    }

    //-------------------------------- Activity가 일시 중지될 때 호출 --------------------------------//
    @Override
    protected void onPause() {
        super.onPause();
        if (processCameraProvider != null) {
            processCameraProvider.unbindAll(); // 앱 일시정지 시 카메라 언바인딩
        }
    }
}