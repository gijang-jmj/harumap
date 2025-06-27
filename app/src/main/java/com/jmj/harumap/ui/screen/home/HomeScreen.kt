package com.jmj.harumap.ui.screen.home

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
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

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen() {
    // 위치 권한 요청을 위한 상태
    val locationPermissionState =
        rememberPermissionState(android.Manifest.permission.ACCESS_FINE_LOCATION)
    val isGranted = locationPermissionState.status.isGranted

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
        if (!isGranted) {
            locationPermissionState.launchPermissionRequest()
        }
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
                    isMyLocationEnabled = isGranted
                ),
                uiSettings = MapUiSettings(
                    tiltGesturesEnabled = false,
                    rotationGesturesEnabled = false,
                    zoomControlsEnabled = false,
                    myLocationButtonEnabled = false
                )
            ) {
                Marker(
                    state = remember { MarkerState(position = gigang) },
                    title = "Singapore",
                    snippet = "Marker in Singapore"
                )
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
                    .padding(16.dp)
                    .padding(bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.End,
            ) {
                SmallFloatingActionButton(onClick = {
                    // 일상 작성 버튼 클릭 시 동작 구현
                    // 예: 일상 작성 화면으로 이동
                }) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "일상작성")
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // 예시: 네비게이션 아이템 3개 (아이콘/텍스트 등)
                    Card { Text("홈", modifier = Modifier.padding(12.dp)) }
                    Card { Text("검색", modifier = Modifier.padding(12.dp)) }
                    Card { Text("내정보", modifier = Modifier.padding(12.dp)) }
                }
            }
        }
    }
}