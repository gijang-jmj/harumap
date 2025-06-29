package com.jmj.harumap.data.model

import android.net.Uri
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.LatLng

/**
 * 이미지 후보 데이터 모델
 *
 * @property uri content:// 형태의 MediaStore URI
 * @property date 원본 Exif 촬영일 (예: "2025:06:28 14:30:00")
 * @property latLng Exif에서 추출한 위치 정보
 * @property thumbnail 이미지에 표시할 마커 아이콘 (BitmapDescriptor)
 */
data class ImageCandidate(
    val uri: Uri,
    val date: String,
    val latLng: LatLng,
    val thumbnail: BitmapDescriptor,
)
