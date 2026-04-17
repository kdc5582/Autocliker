# Auto Clicker 앱 - 통합 및 최적화 가이드

## 1. 프로젝트 구조 최종 확인

### 디렉토리 구조
```
AutoClickerNative/
├── app/
│   ├── src/main/
│   │   ├── java/com/example/autoclicker/
│   │   │   ├── ui/
│   │   │   │   ├── MainActivity.kt
│   │   │   │   ├── MacroRegistrationActivity.kt
│   │   │   │   ├── MacroDetailActivity.kt
│   │   │   │   ├── SettingsActivity.kt
│   │   │   │   └── MacroAdapter.kt
│   │   │   ├── service/
│   │   │   │   ├── AutoClickerAccessibilityService.kt
│   │   │   │   ├── MacroExecutionService.kt
│   │   │   │   └── FloatingButtonService.kt
│   │   │   ├── engine/
│   │   │   │   ├── ImageMatcher.kt
│   │   │   │   ├── ResolutionScaler.kt
│   │   │   │   └── MacroEngine.kt
│   │   │   ├── manager/
│   │   │   │   └── ScreenCaptureManager.kt
│   │   │   ├── util/
│   │   │   │   ├── PermissionHelper.kt
│   │   │   │   └── OpenCVManager.kt
│   │   │   └── data/
│   │   │       ├── Macro.kt
│   │   │       └── MacroRepository.kt
│   │   ├── res/
│   │   │   ├── layout/
│   │   │   ├── drawable/
│   │   │   ├── values/
│   │   │   └── xml/
│   │   └── AndroidManifest.xml
│   ├── build.gradle
│   └── proguard-rules.pro
├── build.gradle
├── settings.gradle
└── README.md
```

## 2. 필수 의존성 확인

### build.gradle (Module: app)에서 확인할 사항

```gradle
dependencies {
    // Core Android
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
    
    // Material Design
    implementation 'com.google.android.material:material:1.9.0'
    
    // Kotlin
    implementation 'org.jetbrains.kotlin:kotlin-stdlib:1.8.0'
    
    // OpenCV
    implementation 'org.opencv:opencv-android:4.8.0'
    
    // Coroutines
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.1'
    
    // Timber (로깅)
    implementation 'com.jakewharton.timber:timber:5.0.1'
    
    // Testing
    testImplementation 'junit:junit:4.13.2'
    androidTestImplementation 'androidx.test.ext:junit:1.1.5'
}
```

## 3. AndroidManifest.xml 검증

### 필수 권한 확인
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
<uses-permission android:name="android.permission.BIND_ACCESSIBILITY_SERVICE" />
```

### 필수 서비스 등록 확인
```xml
<service
    android:name=".service.AutoClickerAccessibilityService"
    android:permission="android.permission.BIND_ACCESSIBILITY_SERVICE"
    android:exported="true">
    <intent-filter>
        <action android:name="android.accessibilityservice.AccessibilityService" />
    </intent-filter>
    <meta-data
        android:name="android.accessibilityservice"
        android:resource="@xml/accessibility_service_config" />
</service>

<service
    android:name=".service.MacroExecutionService"
    android:exported="false" />

<service
    android:name=".service.FloatingButtonService"
    android:exported="false" />
```

## 4. 앱 초기화 순서

### Application 클래스 구현 (권장)

```kotlin
import android.app.Application
import com.example.autoclicker.util.OpenCVManager
import timber.log.Timber

class AutoClickerApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Timber 초기화
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        
        // OpenCV 초기화
        OpenCVManager.initialize(this)
        
        Timber.d("Application initialized")
    }
}
```

### AndroidManifest.xml에 등록
```xml
<application
    android:name=".AutoClickerApplication"
    ...>
</application>
```

## 5. 성능 최적화

### 메모리 최적화
- **이미지 처리**: 큰 이미지는 다운샘플링하여 메모리 사용 최소화
- **비트맵 캐싱**: LRU 캐시를 사용하여 자주 사용되는 이미지 캐싱
- **리소스 해제**: 사용 완료 후 즉시 리소스 해제

### 배터리 최적화
- **화면 캡처 간격**: 최소 200ms 이상의 간격 권장
- **CPU 사용률**: 백그라운드 작업 시 우선순위 낮춤
- **네트워크**: 불필요한 네트워크 요청 제거

### 스레드 관리
- **메인 스레드**: UI 업데이트만 수행
- **백그라운드 스레드**: 이미지 처리, 파일 I/O는 Coroutines 사용
- **서비스**: 포그라운드 서비스로 우선순위 유지

## 6. 테스트 체크리스트

### 기능 테스트
- [ ] 매크로 생성 및 저장
- [ ] 이미지 선택 및 미리보기
- [ ] Threshold 슬라이더 조작
- [ ] 매크로 시작/중지
- [ ] 매크로 편집 및 삭제
- [ ] 설정 페이지 권한 확인

### 권한 테스트
- [ ] AccessibilityService 활성화/비활성화
- [ ] 배터리 최적화 제외 설정
- [ ] 오버레이 권한 확인
- [ ] 저장소 권한 확인

### 성능 테스트
- [ ] 메모리 누수 확인 (Android Profiler)
- [ ] CPU 사용률 확인
- [ ] 배터리 소비 확인
- [ ] 프레임 드롭 확인

### 호환성 테스트
- [ ] Android 12 이상에서 테스트
- [ ] Galaxy S25 Ultra에서 테스트
- [ ] 다양한 화면 해상도에서 테스트
- [ ] 다크 모드 호환성 확인

## 7. 로깅 및 디버깅

### Timber 로깅 레벨

```kotlin
// DEBUG: 개발 중 상세 정보
Timber.tag(TAG).d("Debug message")

// INFO: 중요한 정보
Timber.tag(TAG).i("Info message")

// WARNING: 경고
Timber.tag(TAG).w("Warning message")

// ERROR: 에러
Timber.tag(TAG).e(exception, "Error message")
```

### Android Profiler 사용
1. Android Studio → Tools → Profiler
2. CPU, Memory, Network 탭 모니터링
3. 병목 지점 식별 및 최적화

## 8. 빌드 최적화

### ProGuard 규칙 (proguard-rules.pro)

```proguard
# OpenCV 보존
-keep class org.opencv.** { *; }
-dontwarn org.opencv.**

# Timber 보존
-keep class timber.log.Timber { *; }
-dontwarn timber.log.Timber

# 앱 클래스 보존
-keep class com.example.autoclicker.** { *; }
-dontwarn com.example.autoclicker.**
```

### 릴리스 빌드 설정

```gradle
release {
    minifyEnabled true
    shrinkResources true
    proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
}
```

## 9. 배포 전 체크리스트

- [ ] 모든 권한 요청 구현 완료
- [ ] 에러 처리 및 예외 처리 완료
- [ ] 로깅 시스템 구현 완료
- [ ] 성능 테스트 완료
- [ ] 호환성 테스트 완료
- [ ] 보안 검토 완료
- [ ] 프라이버시 정책 검토
- [ ] 앱 아이콘 및 스플래시 화면 준비
- [ ] 앱 설명 및 스크린샷 준비
- [ ] 버전 번호 설정 (versionCode, versionName)

## 10. 문제 해결

### 일반적인 문제

**문제**: AccessibilityService가 작동하지 않음
- **해결**: 설정 → 접근성 → Auto Clicker 활성화 확인

**문제**: 이미지 인식이 안 됨
- **해결**: Threshold 값 조정, 이미지 해상도 확인

**문제**: 앱이 자주 종료됨
- **해결**: 메모리 누수 확인, 배터리 최적화 제외 설정

**문제**: 플로팅 버튼이 표시되지 않음
- **해결**: 오버레이 권한 확인, 다른 앱의 오버레이 비활성화

---

**마지막 업데이트**: 2026년 4월 17일
**작성자**: Manus AI
