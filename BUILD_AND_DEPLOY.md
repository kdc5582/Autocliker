# Auto Clicker - APK 빌드 및 배포 가이드

## 1. 빌드 전 준비

### 1.1 버전 정보 설정

**app/build.gradle**에서 버전 정보 업데이트:

```gradle
android {
    compileSdkVersion 34
    
    defaultConfig {
        applicationId "com.example.autoclicker"
        minSdkVersion 24
        targetSdkVersion 34
        versionCode 1
        versionName "1.0.0"
    }
}
```

### 1.2 앱 서명 키 생성

Android Studio에서 앱 서명 키를 생성합니다:

1. **Build** → **Generate Signed Bundle / APK**
2. **APK** 선택
3. **Create new...** 클릭
4. 다음 정보 입력:
   - **Key store path**: 키스토어 파일 저장 경로
   - **Key store password**: 키스토어 비밀번호
   - **Key alias**: 키 별칭 (예: `autoclicker-key`)
   - **Key password**: 키 비밀번호
   - **Validity (years)**: 25년 이상 권장

**중요**: 키스토어 파일을 안전한 위치에 백업하세요!

### 1.3 서명 설정 저장

**app/build.gradle**에 서명 설정 추가:

```gradle
android {
    signingConfigs {
        release {
            storeFile file("path/to/keystore.jks")
            storePassword "your_keystore_password"
            keyAlias "autoclicker-key"
            keyPassword "your_key_password"
        }
    }
    
    buildTypes {
        release {
            signingConfig signingConfigs.release
            minifyEnabled true
            shrinkResources true
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
}
```

## 2. APK 빌드

### 2.1 Android Studio에서 빌드

1. **Build** → **Build Bundle(s) / APK(s)** → **Build APK(s)**
2. 빌드 완료 후 **Locate** 클릭
3. APK 파일 위치: `app/release/app-release.apk`

### 2.2 명령줄에서 빌드

```bash
# 프로젝트 디렉토리로 이동
cd /path/to/AutoClickerNative

# 릴리스 APK 빌드
./gradlew assembleRelease

# 빌드된 APK 확인
ls -lh app/build/outputs/apk/release/
```

### 2.3 빌드 결과 확인

```bash
# APK 파일 정보 확인
file app/build/outputs/apk/release/app-release.apk

# APK 크기 확인
du -h app/build/outputs/apk/release/app-release.apk

# APK 서명 확인
jarsigner -verify -verbose -certs app/build/outputs/apk/release/app-release.apk
```

## 3. 기기에 설치

### 3.1 Galaxy S25 Ultra에 설치

#### 방법 1: Android Studio를 통한 설치
1. 기기를 USB로 연결
2. **Run** → **Select Device** → Galaxy S25 Ultra 선택
3. **Run 'app'** 클릭

#### 방법 2: ADB를 통한 설치

```bash
# 기기 연결 확인
adb devices

# APK 설치
adb install -r app/build/outputs/apk/release/app-release.apk

# 설치 확인
adb shell pm list packages | grep autoclicker
```

#### 방법 3: 파일 전송을 통한 설치
1. APK 파일을 기기의 Downloads 폴더로 전송
2. 기기에서 파일 관리자 열기
3. APK 파일 탭하여 설치

### 3.2 설치 후 권한 설정

앱 설치 후 다음 권한을 설정해야 합니다:

1. **AccessibilityService 활성화**
   - 설정 → 접근성 → Auto Clicker 활성화

2. **배터리 최적화 제외**
   - 설정 → 배터리 및 기기 관리 → 배터리 최적화 → Auto Clicker 제외

3. **오버레이 권한**
   - 설정 → 앱 → 특수 앱 액세스 → 다른 앱 위에 표시 → Auto Clicker 활성화

## 4. 테스트

### 4.1 기본 기능 테스트

```bash
# 앱 실행
adb shell am start -n com.example.autoclicker/.ui.MainActivity

# 앱 로그 확인
adb logcat | grep "AutoClicker"

# 앱 종료
adb shell am force-stop com.example.autoclicker
```

### 4.2 성능 모니터링

```bash
# CPU 사용률 확인
adb shell top -n 1 | grep autoclicker

# 메모리 사용량 확인
adb shell dumpsys meminfo com.example.autoclicker

# 배터리 소비 확인
adb shell dumpsys batterystats | grep -A 50 "com.example.autoclicker"
```

### 4.3 권한 확인

```bash
# 부여된 권한 확인
adb shell pm list permissions -d | grep autoclicker

# 특정 권한 확인
adb shell pm dump com.example.autoclicker | grep -A 10 "android.permission"
```

## 5. 배포

### 5.1 Google Play Store 배포 (선택사항)

1. **Google Play Console** 접속 (https://play.google.com/console)
2. 새 앱 생성
3. 앱 정보 입력:
   - 앱 이름
   - 설명
   - 스크린샷 (5개 이상)
   - 아이콘 (512x512px)
   - 배너 (1024x500px)
4. 콘텐츠 등급 설정
5. 대상 국가 선택
6. 가격 설정
7. APK 업로드
8. 검토 요청

### 5.2 직접 배포 (개인 사용)

#### 방법 1: APK 파일 공유
```bash
# APK 파일을 클라우드 스토리지에 업로드
# (Google Drive, OneDrive, Dropbox 등)

# 다른 사용자가 다운로드 후 설치
```

#### 방법 2: QR 코드 생성
```bash
# APK 파일의 다운로드 링크로 QR 코드 생성
# (QR 코드 생성기 사용)

# 사용자가 QR 코드 스캔 후 설치
```

## 6. 업데이트

### 6.1 새 버전 빌드

1. **versionCode** 증가 (예: 1 → 2)
2. **versionName** 업데이트 (예: "1.0.0" → "1.1.0")
3. 변경 사항 정리
4. APK 빌드

```gradle
defaultConfig {
    versionCode 2
    versionName "1.1.0"
}
```

### 6.2 기기에 업데이트 설치

```bash
# 기존 앱 제거
adb uninstall com.example.autoclicker

# 새 버전 설치
adb install -r app/build/outputs/apk/release/app-release.apk
```

## 7. 문제 해결

### 빌드 실패

**문제**: `Gradle build failed`
```bash
# 해결: Gradle 캐시 삭제
./gradlew clean build
```

**문제**: `OpenCV not found`
```bash
# 해결: OpenCV 라이브러리 다시 설치
./gradlew build --refresh-dependencies
```

### 설치 실패

**문제**: `INSTALL_FAILED_INVALID_APK`
```bash
# 해결: APK 서명 확인
jarsigner -verify app/build/outputs/apk/release/app-release.apk
```

**문제**: `INSTALL_FAILED_INSUFFICIENT_STORAGE`
```bash
# 해결: 기기 저장소 확인
adb shell df /data
```

### 런타임 에러

**문제**: `AccessibilityService not working`
- 설정에서 수동으로 활성화 필요

**문제**: `Image recognition fails`
- Threshold 값 조정
- 이미지 해상도 확인

**문제**: `App crashes on startup`
```bash
# 로그 확인
adb logcat | grep "AutoClicker"
```

## 8. 배포 체크리스트

- [ ] 버전 정보 설정 완료
- [ ] 앱 서명 키 생성 완료
- [ ] 릴리스 빌드 성공
- [ ] APK 파일 크기 확인 (< 100MB 권장)
- [ ] 기기에 설치 완료
- [ ] 권한 설정 완료
- [ ] 기본 기능 테스트 완료
- [ ] 성능 테스트 완료
- [ ] 호환성 테스트 완료
- [ ] 배포 준비 완료

## 9. 보안 주의사항

- **키스토어 파일**: 안전한 위치에 백업 (절대 공개하지 마세요)
- **비밀번호**: 강력한 비밀번호 사용
- **코드 난독화**: ProGuard/R8로 코드 난독화
- **민감한 정보**: 하드코딩된 비밀번호나 API 키 제거
- **권한 최소화**: 필요한 권한만 요청

## 10. 성능 팁

- **APK 크기 최소화**: 불필요한 리소스 제거
- **메모리 최적화**: 이미지 다운샘플링
- **배터리 최적화**: 백그라운드 작업 최소화
- **네트워크**: 불필요한 요청 제거

---

**마지막 업데이트**: 2026년 4월 17일
**작성자**: Manus AI
**대상 기기**: Samsung Galaxy S25 Ultra
**최소 Android 버전**: Android 12 (API 31)
