# GitHub 자동 빌드 설정 가이드

## 📋 개요

이 프로젝트는 GitHub Actions를 사용하여 자동으로 APK를 빌드합니다. 코드를 GitHub에 푸시하면 자동으로 빌드가 시작되고, APK 파일이 생성됩니다.

## 🚀 설정 단계

### 1단계: GitHub 저장소 준비

```bash
# 저장소 클론 (이미 생성된 상태)
git clone https://github.com/kdc5582/Autocliker.git
cd Autocliker
```

### 2단계: 로컬 저장소 설정

```bash
# 원격 저장소 추가
git remote add origin https://github.com/kdc5582/Autocliker.git

# 또는 기존 원격 저장소 확인
git remote -v
```

### 3단계: 코드 푸시

```bash
# 모든 파일 추가
git add -A

# 커밋
git commit -m "Initial commit: Auto Clicker app"

# 푸시 (main 또는 master 브랜치)
git push -u origin master
# 또는
git push -u origin main
```

### 4단계: GitHub Actions 확인

1. GitHub 저장소 페이지 방문: https://github.com/kdc5582/Autocliker
2. **Actions** 탭 클릭
3. **Build APK** 워크플로우 실행 상태 확인
4. 빌드 완료 대기 (약 10-15분)

## 📥 APK 다운로드

### 방법 1: Artifacts에서 다운로드 (권장)

1. GitHub 저장소 → **Actions** 탭
2. 최신 **Build APK** 워크플로우 클릭
3. **Artifacts** 섹션에서 **app-release** 다운로드
4. ZIP 파일 압축 해제 → `app-release.apk` 사용

### 방법 2: Releases에서 다운로드

태그를 생성하면 자동으로 Release가 생성됩니다:

```bash
# 태그 생성
git tag v1.0.0

# 태그 푸시
git push origin v1.0.0
```

그러면 GitHub Releases에서 APK를 다운로드할 수 있습니다.

## 🔄 자동 빌드 워크플로우

### 빌드 트리거 조건

다음 경우에 자동으로 빌드가 시작됩니다:

- `main` 브랜치에 푸시
- `master` 브랜치에 푸시
- `develop` 브랜치에 푸시
- Pull Request 생성
- **Actions** 탭에서 수동 실행

### 빌드 과정

1. **JDK 11 설정** (1분)
2. **Android SDK 설치** (3분)
3. **Gradle 빌드** (8-10분)
4. **APK 생성** (1분)
5. **Artifacts 업로드** (1분)

**총 소요 시간**: 약 15분

## 📱 APK 설치

### Galaxy S25 Ultra에 설치

```bash
# APK 다운로드 후
adb install -r app-release.apk

# 또는 파일 관리자에서 직접 설치
```

### 권한 설정 (수동)

설치 후 다음 권한을 활성화해야 합니다:

1. **설정 → 접근성 → Auto Clicker** 활성화
2. **설정 → 배터리 및 기기 관리 → 배터리 최적화 → Auto Clicker 제외**
3. **설정 → 앱 → 특수 앱 액세스 → 다른 앱 위에 표시 → Auto Clicker 활성화**

## 🔧 빌드 실패 시 해결 방법

### 문제: "Build failed"

**해결**:
1. GitHub Actions 로그 확인
2. 프로젝트 파일 검증
3. build.gradle 설정 확인

### 문제: "OpenCV not found"

**해결**:
- build.gradle에서 OpenCV 의존성 확인
- `./gradlew build --refresh-dependencies` 실행

### 문제: "Java version mismatch"

**해결**:
- GitHub Actions에서 Java 11 사용 중
- 로컬 환경에서도 Java 11 사용 권장

## 📊 빌드 상태 확인

### GitHub Actions 페이지

- ✅ **초록색**: 빌드 성공
- ❌ **빨간색**: 빌드 실패
- ⏳ **노란색**: 빌드 진행 중

### 로그 확인

1. 워크플로우 클릭
2. **Build APK** 작업 클릭
3. 각 단계의 로그 확인

## 🔐 보안 주의사항

### 키스토어 파일

- 현재 설정에서는 **디버그 서명**을 사용합니다
- 프로덕션 배포 시 **릴리스 서명 키** 필요
- 서명 키는 **절대 GitHub에 업로드하지 마세요**

### 보안 설정

GitHub Secrets를 사용하여 민감한 정보 관리:

```bash
# GitHub 저장소 Settings → Secrets and variables → Actions
# 다음 정보를 추가할 수 있습니다:
# - KEYSTORE_FILE (Base64 인코딩)
# - KEYSTORE_PASSWORD
# - KEY_ALIAS
# - KEY_PASSWORD
```

## 📝 커밋 메시지 규칙

좋은 커밋 메시지 예시:

```
feat: Add new image matching algorithm
fix: Fix memory leak in ImageMatcher
docs: Update README with new features
refactor: Optimize ResolutionScaler performance
test: Add unit tests for MacroEngine
```

## 🚀 버전 관리

### 버전 번호 업데이트

**app/build.gradle**에서:

```gradle
defaultConfig {
    versionCode 1  // 증가시키기 (1 → 2 → 3 ...)
    versionName "1.0.0"  // 업데이트 (1.0.0 → 1.1.0 → 2.0.0)
}
```

### 태그 생성

```bash
# 새 버전 태그 생성
git tag -a v1.1.0 -m "Release version 1.1.0"

# 태그 푸시
git push origin v1.1.0

# 모든 태그 푸시
git push origin --tags
```

## 📞 문제 해결

### GitHub 푸시 실패

```bash
# 인증 문제 해결
git config --global credential.helper store
git push origin master
```

### 빌드 로그 확인

1. GitHub 저장소 → **Actions** 탭
2. 최신 워크플로우 클릭
3. **Build APK** 작업 클릭
4. 각 단계의 로그 확인

### 로컬 테스트

```bash
# 로컬에서 빌드 테스트
./gradlew clean assembleRelease

# 빌드 결과 확인
ls -lh app/build/outputs/apk/release/
```

## ✅ 체크리스트

- [ ] GitHub 저장소 생성 완료
- [ ] 로컬 저장소 설정 완료
- [ ] 코드 푸시 완료
- [ ] GitHub Actions 워크플로우 실행 확인
- [ ] APK 빌드 완료
- [ ] APK 다운로드 완료
- [ ] Galaxy S25 Ultra에 설치 완료
- [ ] 권한 설정 완료
- [ ] 앱 테스트 완료

---

**마지막 업데이트**: 2026년 4월 17일
**작성자**: Manus AI
**저장소**: https://github.com/kdc5582/Autocliker
