package com.jmj.harumap.data.model

import java.util.UUID

/**
 * 일상 기록(DailyRecord) 데이터 모델
 *
 * @property id 로컬 DB 기준 ID (기본값: UUID)
 * @property imageUri 내부 저장소의 절대 경로나 content URI
 * @property caption 사용자 입력 메모
 * @property lat 위도
 * @property lng 경도
 * @property detailAddress 선택 입력 가능 상세 주소
 * @property datetime ISO 8601 형식 (예: "2025-06-28T14:30:00")
 * @property category 향후 확장용 (예: 맛집/명소, 기본값: "etc")
 * @property isShared 서버 연동 시 확장 (기본값: false)
 * @property createdAt 작성 시간 기준 (기본적으로 datetime과 동일)
 */
data class DailyRecord(
    val id: String = UUID.randomUUID().toString(),
    val imageUri: String,
    val caption: String,
    val lat: Double,
    val lng: Double,
    val detailAddress: String?,
    val datetime: String,
    val category: String = "etc",
    val isShared: Boolean = false,
    val createdAt: String = datetime
)
