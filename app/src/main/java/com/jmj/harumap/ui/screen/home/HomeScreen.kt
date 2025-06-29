package com.jmj.harumap.ui.screen.home

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.net.Uri
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import androidx.exifinterface.media.ExifInterface
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.jmj.harumap.R
import com.jmj.harumap.data.model.ImageCandidate

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    // 위치 권한 요청을 위한 상태
    val locationPermissionState =
        rememberPermissionState(android.Manifest.permission.ACCESS_FINE_LOCATION)

    // 이미지 권한 요청을 위한 상태 (Android 14 이상에서 필요)
    val mediaPermissionState =
        rememberPermissionState(android.Manifest.permission.READ_MEDIA_IMAGES)

    // 다크 모드에 따라 지도 스타일을 설정
    val context = LocalContext.current
    val isDarkTheme = isSystemInDarkTheme()
    val mapStyleJson = if (isDarkTheme) {
        context.resources.openRawResource(R.raw.map_style_dark).bufferedReader()
            .use { it.readText() }
    } else null
    val mapStyleOptions = mapStyleJson?.let { MapStyleOptions(it) }

    // HomeScreen이 켜지면 바로 권한 요청
    LaunchedEffect(Unit) {
        if (!locationPermissionState.status.isGranted) {
            locationPermissionState.launchPermissionRequest()
        }
        if (!mediaPermissionState.status.isGranted) {
            mediaPermissionState.launchPermissionRequest()
        }
    }

    // 이미지 후보 리스트를 상태로 관리
    val (imageCandidateList, setImageCandidateList) = androidx.compose.runtime.remember {
        androidx.compose.runtime.mutableStateOf(
            listOf<ImageCandidate>()
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
    ) { innerPadding ->
        val gigang = LatLng(35.248844, 129.218586)
        val cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(gigang, 10f)
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    bottom = innerPadding.calculateBottomPadding(),
                    start = innerPadding.calculateStartPadding(LayoutDirection.Ltr),
                    end = innerPadding.calculateEndPadding(LayoutDirection.Ltr)
                )
        ) {
            // [구글 맵 컴포넌트]
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    isBuildingEnabled = true,
                    mapStyleOptions = mapStyleOptions,
                    isMyLocationEnabled = locationPermissionState.status.isGranted
                ),
                uiSettings = MapUiSettings(
                    tiltGesturesEnabled = false,
                    rotationGesturesEnabled = false,
                    zoomControlsEnabled = false,
                    myLocationButtonEnabled = false
                )
            ) {
                // imageCandidateList의 각 위치에 마커 추가
                imageCandidateList.forEach { candidate ->
                    Marker(
                        state = MarkerState(position = candidate.latLng),
                        title = candidate.date,
                        snippet = candidate.uri.toString(),
                        icon = candidate.thumbnail,
                    )
                }
            }

            // [날짜 선택 영역]
            // 가장 첫 번째 요소는 현재 달 -> 클릭 시 캘린더 오픈 날짜 선택 가능하게 -> 스크롤 시에도 고정 요소
            // 나머지 요소는 오늘 달 기준의 모든 날짜 일 표시
            // gap 은 8dp로 설정
            // width 는 화면 전체 너비 (fillMaxWidth)로 설정 (초과 시 스크롤 가능하게)
            // 일요일은 빨간색, 토요일은 파란색으로 표시
            // 미래는 선택 불가
            // 날짜 선택 시 해당 날짜로 오토 스크롤
            val today = java.time.LocalDate.now()
            val currentMonth = today.monthValue
            val currentYear = today.year
            val daysInMonth = java.time.YearMonth.of(currentYear, currentMonth).lengthOfMonth()
            val days =
                (1..daysInMonth).map { java.time.LocalDate.of(currentYear, currentMonth, it) }
            val selectedDate =
                remember { mutableStateOf(today) }
            val openCalendar =
                remember { mutableStateOf(false) }
            val listState = rememberLazyListState()

            // 날짜가 바뀔 때마다 오토 스크롤 (센터 정렬)
            val density = LocalDensity.current
            val itemSizePx = with(density) { 48.dp.roundToPx() }
            val rowWidthPx =
                remember { androidx.compose.runtime.mutableIntStateOf(0) }

            LaunchedEffect(selectedDate.value, rowWidthPx.intValue) {
                val idx = days.indexOfFirst { it == selectedDate.value }
                if (idx >= 0 && rowWidthPx.intValue > 0) {
                    val offset = (rowWidthPx.intValue / 2) - (itemSizePx / 2)
                    listState.animateScrollToItem(idx, -offset)
                }
            }

            if (openCalendar.value) {
                DatePickerDialog(
                    onDismissRequest = { openCalendar.value = false },
                    confirmButton = {
                        TextButton(onClick = {
                            openCalendar.value = false
                        }) {
                            Text("확인")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            openCalendar.value = false
                        }) {
                            Text("취소")
                        }
                    }
                ) {
                    val datePickerState = rememberDatePickerState(
                        initialSelectedDateMillis = selectedDate.value.toEpochDay() * 24 * 60 * 60 * 1000
                    )
                    DatePicker(
                        state = datePickerState
                    )
                    // 확인 버튼에서 날짜 선택 처리
                    TextButton(onClick = {
                        val millis = datePickerState.selectedDateMillis
                        if (millis != null) {
                            val picked = java.time.Instant.ofEpochMilli(millis)
                                .atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                            if (!picked.isAfter(today)) {
                                selectedDate.value = picked
                            }
                        }
                        openCalendar.value = false
                    }) {
                        Text("확인")
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .padding(top = innerPadding.calculateTopPadding()),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(
                    8.dp
                )
            ) {
                // 현재 달(월) 표시, 클릭 시 캘린더 오픈 (고정)
                Card(onClick = { openCalendar.value = true }) {
                    Text(
                        text = "${selectedDate.value.monthValue}월",
                        modifier = Modifier.padding(16.dp)
                    )
                }
                // LazyRow로 날짜들만 스크롤
                LazyRow(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .onSizeChanged { rowWidthPx.intValue = it.width },
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(days.size) { idx ->
                        val date = days[idx]
                        val dayOfWeek = date.dayOfWeek
                        val isFuture = date.isAfter(today)
                        val textColor = when (dayOfWeek) {
                            java.time.DayOfWeek.SUNDAY -> androidx.compose.ui.graphics.Color(
                                0xFFF97066
                            ) // 연한 레드
                            java.time.DayOfWeek.SATURDAY -> androidx.compose.ui.graphics.Color(
                                0xFF6CB6F7
                            ) // 연한 블루
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                        val dayOfWeekKorean = when (date.dayOfWeek) {
                            java.time.DayOfWeek.MONDAY -> "월"
                            java.time.DayOfWeek.TUESDAY -> "화"
                            java.time.DayOfWeek.WEDNESDAY -> "수"
                            java.time.DayOfWeek.THURSDAY -> "목"
                            java.time.DayOfWeek.FRIDAY -> "금"
                            java.time.DayOfWeek.SATURDAY -> "토"
                            java.time.DayOfWeek.SUNDAY -> "일"
                        }
                        Card(
                            enabled = !isFuture,
                            onClick = {
                                if (!isFuture) {
                                    selectedDate.value = date
                                }
                            },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            modifier = Modifier
                                .width(48.dp)
                                .height(48.dp)
                                .then(
                                    if (selectedDate.value == date) Modifier.border(
                                        width = 2.dp,
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = MaterialTheme.shapes.medium
                                    ) else Modifier
                                ),
                            content = {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        style = MaterialTheme.typography.titleMedium,
                                        text = "${date.dayOfMonth}",
                                        color = textColor,
                                    )
                                    Text(
                                        style = MaterialTheme.typography.labelMedium,
                                        text = dayOfWeekKorean,
                                        color = textColor,
                                    )
                                }
                            }
                        )
                    }
                }
            }

            // [하단 네비게이션 영역]
            // 기록 추가 버튼
            // 추후 Navigation Compose와 연동 가능하게 마크업
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                horizontalAlignment = Alignment.End,
            ) {
                SmallFloatingActionButton(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary, onClick = {
                        // MediaStore에서 이미지 URI 리스트 가져오기
                        val uriList = getUriListFromMediaStore(context, "2025-06-13")

                        android.util.Log.d(
                            "MediaStore",
                            "uriList: ${uriList.size}"
                        )

                        // Exif 정보로 후보군 필터링
                        val imageCandidateList = getImageCandidateList(context, uriList)

                        android.util.Log.d(
                            "MediaStore",
                            "imageCandidateList: ${imageCandidateList.size}"
                        )

                        // 이미지 후보 리스트 업데이트
                        setImageCandidateList(imageCandidateList)
                    }) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "일상작성")
                }
            }
        }
    }
}

/**
 * MediaStore에서 지정된 날짜의 이미지 URI 리스트를 가져오는 함수
 *
 * @param context Context 객체
 * @param date ISO 8601 형식의 날짜 문자열 (예: "2025-06-13")
 * @return 해당 날짜에 촬영된 이미지들의 URI 리스트
 */
fun getUriListFromMediaStore(context: Context, date: String): List<android.net.Uri> {
    val projection = arrayOf(
        android.provider.MediaStore.Images.Media._ID,
        android.provider.MediaStore.Images.Media.DATE_ADDED
    )
    val selection =
        "date(datetime(${android.provider.MediaStore.Images.Media.DATE_ADDED}, 'unixepoch', 'localtime')) = ?"
    val selectionArgs = arrayOf(date)
    val cursor = context.contentResolver.query(
        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        projection,
        selection,
        selectionArgs,
        "${android.provider.MediaStore.Images.Media.DATE_ADDED} DESC"
    )
    val uriList = mutableListOf<android.net.Uri>()

    cursor?.use {
        val idColumn =
            it.getColumnIndexOrThrow(android.provider.MediaStore.Images.Media._ID)
        while (it.moveToNext()) {
            val id = it.getLong(idColumn)
            val contentUri = android.content.ContentUris.withAppendedId(
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id
            )
            uriList.add(contentUri)
        }
    }

    return uriList
}

/**
 * MediaStore에서 가져온 이미지 URI 리스트를 기반으로 Exif 정보를 읽어
 * 이미지 후보 리스트를 생성하는 함수
 *
 * @param context Context 객체
 * @param uriList MediaStore에서 가져온 이미지 URI 리스트
 * @return Exif 정보를 포함한 이미지 후보 리스트
 */
fun getImageCandidateList(context: Context, uriList: List<android.net.Uri>): List<ImageCandidate> {
    return uriList.mapNotNull { uri ->
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            inputStream?.use { stream ->
                val exif = ExifInterface(stream)
                val datetime =
                    exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL)
                        ?: return@mapNotNull null
                val latLong = exif.getLatLong() ?: doubleArrayOf(
                    37.243075, 131.866817
                )

                // custom marker 생성
                val thumbnailBitmap = createCustomThumbnailBitmap(context, uri)
                val markerBitmap = createCustomMarkerBitmap(context, thumbnailBitmap)
                val descriptor = BitmapDescriptorFactory.fromBitmap(markerBitmap)

                ImageCandidate(
                    uri = uri,
                    date = datetime,
                    latLng = LatLng(latLong[0], latLong[1]),
                    thumbnail = descriptor
                )
            }
        } catch (e: Exception) {
            android.util.Log.e("MediaStore", "Error reading Exif for $uri", e)
            return@mapNotNull null
        }
    }
}

/**
 * 주어진 Bitmap에 대해 둥근 모서리를 적용한 새로운 Bitmap을 생성하는 함수
 *
 * @param bitmap 원본 Bitmap
 * @param radius 둥근 모서리의 반지름
 * @return 둥근 모서리가 적용된 Bitmap
 */
fun getRoundedCornerBitmap(bitmap: Bitmap, radius: Float): Bitmap {
    val output = createBitmap(bitmap.width, bitmap.height)
    val canvas = Canvas(output)
    val paint = Paint()
    val rect = Rect(0, 0, bitmap.width, bitmap.height)
    val rectF = RectF(rect)

    paint.isAntiAlias = true
    canvas.drawARGB(0, 0, 0, 0)
    canvas.drawRoundRect(rectF, radius, radius, paint)
    paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
    canvas.drawBitmap(bitmap, rect, rect, paint)

    return output
}

/**
 * 주어진 Context와 URI를 기반으로 커스텀 썸네일 Bitmap을 생성하는 함수
 *
 * @param context Context 객체
 * @param uri 이미지의 URI
 * @return 커스텀 썸네일 Bitmap
 */
fun createCustomThumbnailBitmap(
    context: Context,
    uri: Uri,
): Bitmap {
    val thumbnailView = LayoutInflater.from(context).inflate(R.layout.custom_thumbnail, null)
    val imageView = thumbnailView.findViewById<ImageView>(R.id.thumbnail_image)

    imageView.setImageURI(uri)

    thumbnailView.measure(
        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
    )
    thumbnailView.layout(0, 0, thumbnailView.measuredWidth, thumbnailView.measuredHeight)

    val finalBitmap = createBitmap(thumbnailView.measuredWidth, thumbnailView.measuredHeight)
    val canvas = Canvas(finalBitmap)
    thumbnailView.draw(canvas)

    return finalBitmap
}

/**
 * 주어진 Context와 Bitmap을 기반으로 커스텀 마커 Bitmap을 생성하는 함수
 *
 * @param context Context 객체
 * @param bitmap 원본 Bitmap
 * @return 커스텀 마커 Bitmap
 */
fun createCustomMarkerBitmap(context: Context, bitmap: Bitmap): Bitmap {
    val markerView = LayoutInflater.from(context).inflate(R.layout.custom_marker, null)
    val imageView = markerView.findViewById<ImageView>(R.id.marker_image)
    val roundedBitmap = getRoundedCornerBitmap(bitmap, 16.0f)

    imageView.setImageBitmap(roundedBitmap)

    markerView.measure(
        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
    )
    markerView.layout(0, 0, markerView.measuredWidth, markerView.measuredHeight)

    val bitmap = createBitmap(markerView.measuredWidth, markerView.measuredHeight)
    val canvas = Canvas(bitmap)
    markerView.draw(canvas)

    return bitmap
}
