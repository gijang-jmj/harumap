
# 📄 하루지도 MVP API 명세서 (Android/Kotlin 기반)

## 📌 개요

- **앱 이름**: 하루지도 (영문 식별자: `harumap`)
- **플랫폼**: Android (Kotlin + Jetpack Compose)
- **백엔드**: Firebase Functions (Firestore 접근은 API를 통해서만)
- **인증 방식**: Firebase Auth (ID 토큰 기반)

---

## 📝 1. 일상 기록 API

### 📤 POST /records

- **설명**: 하루 기록 업로드
- **요청 본문** (`application/json`):

```json
{
  "imageUrl": "https://...",
  "thumbnailUrl": "https://...",
  "caption": "오늘의 한 컷",
  "location": { "lat": 37.56, "lng": 126.97 },
  "detailAddress": "서울특별시 중구 세종대로 110",
  "datetime": "2025-06-26T14:30:00",
  "category": "etc",
  "isShared": false
}
```

- **응답**:

```json
{ "recordId": "abc123" }
```

---

### 📥 GET /records/mine?date=2025-06-26

- **설명**: 특정 날짜의 내 하루 기록 조회 (하루지도용)
- **쿼리 파라미터**: `date=YYYY-MM-DD`
- **응답**:

```json
[
  {
    "recordId": "abc123",
    "imageUrl": "...",
    "caption": "...",
    "location": { "lat": ..., "lng": ... },
    "datetime": "...",
    "thumbnailUrl": "...",
    "detailAddress": "..."
  }
]
```

---

### 📥 GET /records/mine/month?year=2025&month=06

- **설명**: 특정 월의 내 기록 조회 (달지도용)
- **응답**: 위와 동일한 리스트 형식

---

## 👥 2. 친구 공유 기록 API (확장용)

### 📥 GET /records/friends/day?date=2025-06-26

- **설명**: 친구들의 공유된 하루 기록 조회
- **동작 흐름**:
  1. `users/{me}.friends` 필드 조회
  2. 각 친구의 `isShared: true` 조건을 만족하는 기록만 반환

---

## 🤝 3. 사용자 API

### 📥 GET /user/profile

- **설명**: 내 프로필 정보 조회
- **응답**:

```json
{
  "uid": "...",
  "nickname": "민종",
  "profileImageUrl": "...",
  "friends": ["uid1", "uid2"]
}
```

---

### 📤 POST /user/profile

- **설명**: 닉네임 및 프로필 이미지 수정
- **요청**:

```json
{
  "nickname": "민종",
  "profileImageUrl": "https://..."
}
```

---

### 📤 POST /user/friends

- **설명**: 친구 추가 요청
- **요청**:

```json
{ "friendUid": "user_b" }
```

---

## 🔧 기타 구현 메모

| 항목       | 설명 |
|------------|------|
| 인증       | Firebase Auth 사용 (`context.auth.uid`) |
| 기본 쿼리  | `where("uid", "==", uid)` + `where("datetime", ">=", ...)` |
| 입력 검증  | Functions 내부에서 날짜/글자수/위치 등 유효성 검사 수행 |

---

## ✅ 요약

| API                             | 설명                         |
|----------------------------------|------------------------------|
| `POST /records`                 | 하루 기록 업로드              |
| `GET /records/mine`             | 하루지도용 내 기록 조회        |
| `GET /records/mine/month`       | 달지도용 내 기록 조회         |
| `GET /records/friends/day`      | 친구 공유 기록 조회 (확장용)   |
| `GET /user/profile`             | 내 프로필 정보 조회           |
| `POST /user/profile`            | 닉네임 및 이미지 수정         |
| `POST /user/friends`            | 친구 추가 요청                |
