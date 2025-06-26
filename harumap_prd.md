
# 📅 하루지도 MVP 기획서 (Android/Kotlin 버전)

## ✨ 제품 개요

- **제품명**: 하루지도 (영문 식별자: `harumap`)
- **목표**: 사용자의 하루를 사진, 위치, 텍스트로 간단히 기록하고 지도 위에 시각적으로 남김
- **MVP 목적**: 정보 기반 없이도 간단하게 기록을 남기고 공유 가능한 하이브리드 지도 기반 SNS 구축
- **수익모델**: 광고 기반 (AdMob 등)

---

## 📈 핵심 가치 제안

| 기능               | 설명                                                  |
|------------------|-----------------------------------------------------|
| 🌟 하루지도 기록       | 사용자 위치 기반 하루 기록을 지도에 남김                             |
| 📅 달지도 보기        | 월 단위로 기록을 풍성하게 시각화                                  |
| 🚪 과거 기록 가능      | 사진 메타데이터 기반으로 과거 기록도 등록 가능                          |
| 🤝 친구 기반 공유 (확장) | 친구 추가 기반으로 공개된 기록만 보여줌 (`isShared` 기반)           |
| 📍 위치 기반 감성 기록   | 썸네일 + 간단한 텍스트로 직관적 지도 일기 구현                         |

---

## 🚀 MVP 핵심 기능

### 1. 🌐 하루지도 (일 단위)
- 내 기록만 지도에 표시
- 마커 위치 기준으로 지도 뷰포트 자동 조정

### 2. 🗓️ 달지도 (월 단위)
- 한 달간의 기록 전체 지도에 표시
- 줌아웃 상태에서 클러스터링 + 썸네일 오버레이 지원

### 3. 🗐️ 과거 기록 등록
- Exif 메타데이터 기반 날짜/위치 자동 추출
- 날짜·시간 수동 입력도 허용

### 4. 🔖 카테고리 구조 (MVP UI 제외)
- 필드: `etc`, `맛집`, `명소` 등
- UI 필터링 기능은 차후 확장 시 추가

---

## 🛠️ 기술 스택

### 📱 모바일 플랫폼

| 항목     | 선택                         | 설명 |
|----------|------------------------------|------|
| 언어     | Kotlin                       | Android 공식 언어 |
| UI 프레임워크 | Jetpack Compose               | 선언형 UI, 최신 표준 |
| 아키텍처 | MVVM + Clean Architecture     | ViewModel / UseCase / Repository 분리 |
| 상태관리 | StateFlow + collectAsState() | Compose 연동에 적합 |
| DI       | Hilt                         | Android 공식 DI, Firebase/VM 연동 우수 |
| 이미지 선택 | TedImagePicker                | 커스터마이징 + 권한 자동 처리 |
| 이미지 압축 | Compressor by zetbaitsu        | 70% 품질 압축, 설정 간단 |
| 이미지 로딩 | Coil                         | Compose 전용 경량 이미지 로더 |
| 메타데이터 추출 | ExifInterface                  | 촬영 날짜, 위치 정보 추출용 |
| 위치 권한 처리 | Accompanist Permissions        | Compose에서 권한 UI 처리 |
| 지도 SDK | Google Maps SDK for Android | 지도 기능 구현 표준 |
| 마커 클러스터링 | Maps Utils SDK                 | 클러스터링 + 커스텀 마커 지원 |
| 날짜 처리 | java.time (`LocalDate`, etc) | Android 26+ 또는 desugaring 활용 |
| 백엔드   | Firebase Auth / Firestore / Storage / Functions | Functions 기반 API 호출 구조 |

### 🔧 minSdk / targetSdk

```kotlin
minSdkVersion = 24      // Android 7.0 (Nougat)
targetSdkVersion = 34   // Android 14
```

---

## 🧱 디렉토리 구조 (MVP 기준)

```
com/yourdomain/harumap/
├── di/                    # Hilt 모듈
├── data/
│   ├── model/             # DTO (Firestore 등)
│   ├── remote/            # Firebase API 호출
│   └── repository/        # Repository 구현
├── domain/
│   ├── model/             # Entity (불변 상태)
│   └── usecase/           # UseCase 계층
├── presentation/
│   ├── home/              # 지도 화면
│   ├── record/            # 기록 등록
│   ├── detail/            # 상세 뷰
│   └── components/        # 공통 UI 컴포넌트
├── navigation/            # NavHost 라우팅
├── ui/theme/              # MaterialTheme, Typography 등
├── utils/                 # Exif, Uri/File 변환 등
└── MainActivity.kt        # 앱 진입점
```

---

## 🔒 데이터 구조 (Firestore)

### 📁 users

```json
{
  "uid": "user_abc",
  "nickname": "민종",
  "profileImageUrl": "...",
  "friends": ["uid1", "uid2"]
}
```

### 📁 records

```json
{
  "uid": "user_abc",
  "imageUrl": "...",
  "thumbnailUrl": "...",
  "caption": "카페에서 여유",
  "location": { "lat": 37.56, "lng": 126.97 },
  "detailAddress": "서울 중구 ...",
  "datetime": "2025-06-26T14:30:00",
  "category": "etc",
  "isShared": false,
  "createdAt": Timestamp
}
```

---

## 🧠 최적화 전략

| 항목       | 전략 |
|------------|------|
| 하루지도     | 로컬 우선 → 없으면 Functions API 호출 |
| 달지도      | 항상 서버 호출로 조회 |
| 뷰포트 조정  | 마커 위치 기준 클라이언트에서 계산 |
| 마커 클러스터링 | Maps Utils + 커스텀 렌더러 + 썸네일 Stack 처리 |

---

## 📅 MVP 마일스톤 예시

| 주차  | 목표 |
|-------|------|
| 1주차 | UI 설계 (Figma), 프로젝트 구조 세팅 |
| 2주차 | 기록 등록 화면, 이미지 처리, Firebase 연동 |
| 3주차 | 지도 뷰 + 클러스터링, 하루지도 표시 |
| 4주차 | 달지도 구현, 최적화, 출시 준비 |

---

## ✉️ API 명세

→ 별도 문서 참고: harumap_api_spec.md
