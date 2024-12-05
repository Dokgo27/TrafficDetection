# TrafficDetection

### 2024 학사학위논문 : 편의점 이상행동 감지 시스템
![image](https://github.com/user-attachments/assets/89382f32-ac08-43ac-a85e-b881f0057091)

---

## 프로젝트 개요
**TrafficDetection**는 교통위반상황을 탐지하고 분류하는 시스템입니다. <br>
해당 시스템은 실시간 영상 데이터를 처리하여 교통위반 상황을 탐지하는 기능을 가지고 있습니다.

---

## 시스템 전체 구조도
<img src="https://github.com/user-attachments/assets/dd253838-ef49-4017-9e35-dff9923bac8f" alt="시스템 전체 구조도" width="600"/>

---

## 학습 모델

### **LRCN(Long-term Recurrent Convolutional Network)**
LRCN(Long-term Recurrent Convolutional Network)은 시공간 데이터를 처리하는 데 사용되는 딥러닝 모델<br>
Convolutional Neural Networks (CNN)와 Recurrent Neural Networks (RNN)을 결합 <br>
이미지의 공간적 정보와 시계열 데이터의 시간적 정보를 동시에 학습 가능 <br>
#### 자주 쓰이는 분야
<li>동영상 데이터의 행동 인식
<li>비디오 분류
<li>자율 주행<br>

#### LRCN 모델 구조:
![image](https://github.com/user-attachments/assets/aa394148-80ab-4898-9695-ea0332404598)

---
## 주요 기능
1. **실시간 교통 위반 상황 감지**:
   - 교통위반에 대해 실시간 영상 처리를 통한 위반을 분류합니다 (classifier: 신호위반, 중앙선 침범, 진로변경위반)
2. **교통 위반 상황에 대한 목록 확인**:
   - 위반상황이 발생하는 즉시 DB에 저장하고 추후 목록으로 확인 가능
---

## 기대 효과
- **운영 효율성 개선**: 알림 시스템을 구축하여 위반상황에 대한 효율적인 관리 시스템 구축 가능.

---

## 개발 환경
- **모델 학습 프로그래밍 언어**: Python
- **앱 프로그래밍 언어**: Java
- **모델 학습 프레임워크**: TensorFlow
- **서버 프레임워크**: Spring
- **DB** : MariaDB
- **추론 환경**: NVIDIA GPU
- **데이터**: AI HUB 교통법규 위반 상황 데이터

---
