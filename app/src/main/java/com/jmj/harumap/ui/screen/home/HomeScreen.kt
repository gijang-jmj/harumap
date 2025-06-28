package com.jmj.harumap.ui.screen.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.exifinterface.media.ExifInterface
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
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
@OptIn(ExperimentalPermissionsApi::class)
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
                        snippet = candidate.uri.toString()
                    )
                }
            }

            // 상단 safety area에 Row 플로팅 UI
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .padding(top = innerPadding.calculateTopPadding()),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
            ) {
                Card {
                    Text(
                        text = "상단 텍스트",
                        modifier = Modifier.padding(16.dp)
                    )
                }
                Switch(
                    checked = false,
                    onCheckedChange = { /* TODO: 토글 동작 구현 */ }
                )
            }

            // 하단 네비게이션 영역 (추후 Navigation Compose와 연동 가능하게 마크업)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                horizontalAlignment = Alignment.End,
            ) {
                SmallFloatingActionButton(onClick = {
                    // MediaStore에서 이미지 URI 리스트 가져오기
                    val projection = arrayOf(
                        android.provider.MediaStore.Images.Media._ID,
                        android.provider.MediaStore.Images.Media.DATE_ADDED
                    )
                    val cursor = context.contentResolver.query(
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        projection,
                        null,
                        null,
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

                    android.util.Log.d(
                        "MediaStore",
                        "uriList: ${uriList.size}"
                    )

                    // Exif 정보로 후보군 필터링
                    val newList = uriList.take(100).mapNotNull { uri ->
                        try {
                            val inputStream = context.contentResolver.openInputStream(uri)
                            inputStream?.use { stream ->
                                val exif = ExifInterface(stream)
                                val datetime =
                                    exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL)
                                        ?: return@mapNotNull null
                                val latLong = exif.getLatLong() ?: return@mapNotNull null

                                ImageCandidate(
                                    uri = uri,
                                    date = datetime,
                                    latLng = LatLng(latLong[0], latLong[1]),
                                )
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("MediaStore", "Error reading Exif for $uri", e)
                            return@mapNotNull null
                        }
                    }
                    setImageCandidateList(newList)
                    android.util.Log.d(
                        "MediaStore",
                        "imageCandidateList: ${newList.size}"
                    )
                }) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "일상작성")
                }
            }
        }
    }
}