package com.android.exampke.diecipomodori

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition

@Composable
fun SplashScreen(navController: NavController) {
    // Lottie composition from raw resource
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.splashscreen))
    // 애니메이션을 한 번 재생 (3초 정도 재생된다면 애니메이션 자체가 3초 길이여야 함)
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = 1
    )
    // 애니메이션이 끝나면 lobby로 이동
    LaunchedEffect(progress) {
        if (progress >= 1f) {
            navController.navigate("lobby") {
                popUpTo("splash") { inclusive = true }
            }
        }
    }
    LottieAnimation(
        composition = composition,
        progress = { progress },
        modifier = Modifier.fillMaxSize()
    )
}
