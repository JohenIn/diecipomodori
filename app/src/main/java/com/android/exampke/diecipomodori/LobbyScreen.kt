package com.android.exampke.diecipomodori

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.android.exampke.diecipomodori.model.MyDb
import com.android.exampke.diecipomodori.viewmodel.GameViewModel
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

@Composable
fun LobbyScreen(navController: NavController, gameViewModel: GameViewModel) {
    var viewAds by remember { mutableStateOf(false) }
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // BoxWithConstraints의 제약조건을 사용하여 화면 크기를 변수에 할당
        val screenWidth = maxWidth
        val screenHeight = maxHeight
        val context = LocalContext.current
        val vibrate = rememberVibrate()
        // 배경 이미지 (전체 화면)
        ScreenWallpaper(R.drawable.lobby_wallpaper)
        // Play 버튼 이미지 (중앙)
        PlayButton(
            screenHeight,
            gameViewModel,
            navController,
            vibrate,
            modifier = Modifier.align(Alignment.Center)
        )

        TomatoCoinCount(
            gameViewModel,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(top = 30.dp)
        )

        BoxWithConstraints(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(x = -maxWidth * 0.3f)
        ) {
            // 이미지의 하단 20% 영역에 클릭 가능 오버레이 추가
            Image(
                painter = painterResource(R.drawable.board_refilltomatoads),
                contentDescription = "refill tomato Coin",
                colorFilter = if (gameViewModel.usedCoin == 0) {
                    ColorFilter.colorMatrix(ColorMatrix().apply {
                        setToSaturation(0.1f)
                    })
                } else null,
                alpha = if (gameViewModel.usedCoin == 0) 0.7f else 1f,
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .width(maxWidth * 0.13f)
                    .height(maxHeight * 0.3f)
                    .clickable(enabled = gameViewModel.usedCoin > 0) {
                        vibrate()
                        viewAds = true
                    }
                    .background(Color.Transparent)
            )
            //광고
            PreloadedRewardedAd(
                viewAds = viewAds,
                onViewAdsConsumed = { viewAds = false },
                gameViewModel = gameViewModel
            )
        }
        // todo: 앱 출시 직전 지우기
//        Row(modifier = Modifier.align(Alignment.BottomCenter)) {
//            Button(
//                onClick = {
//                    gameViewModel.resetCoins()
//                },
//            ) { Text("Reset Coins") }
//            Button(
//                onClick = {
//                    gameViewModel.increaseUsedCoin()
//                },
//            ) { Text("Minus Coins") }
//        }

        // 상단 로고: 화면 너비의 70%
        Image(
            painter = painterResource(id = R.drawable.mainlobbylogo),
            contentDescription = "top logo",
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .align(Alignment.TopCenter)
                .padding(top = screenHeight * 0.05f) // 예시: 상단 패딩 10% 사용
        )
        // 하단 오른쪽 베스트 스코어 영역
        BestScoreBoard(
            screenWidth,
            modifier = Modifier.align(Alignment.BottomEnd),
            context = context
        )
    }
}

@Composable
private fun PlayButton(
    screenHeight: Dp,
    gameViewModel: GameViewModel,
    navController: NavController,
    vibrate: () -> Unit,
    modifier: Modifier
) {
    Image(
        painter = painterResource(id = R.drawable.board_playbutton),
        contentDescription = "tomato",
        modifier = modifier
            .offset(y = (-screenHeight * (0.18f)))
            .clickable {
                if (gameViewModel.coinsForPlaying > 0) {
                    navController.navigate("game")
                    vibrate()
                } else {
                    vibrate()
                }
            }
    )
}

@Composable
fun ScreenWallpaper(image: Int, modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(id = image),
        contentDescription = "tomato",
        modifier = modifier.fillMaxSize(),
    )
}

@Composable
private fun BestScoreBoard(screenWidth: Dp, context: Context, modifier: Modifier) {
    Box(
        modifier = modifier
            .width(screenWidth * (0.3f))
            .padding(end = screenWidth * (0.05f))
    ) {
        Image(
            painter = painterResource(id = R.drawable.board_bestscore),
            contentDescription = "tomato",
            modifier = Modifier
        )
        BestScore(context, modifier = Modifier.align(Alignment.Center))
    }
}

@Composable
fun TomatoCoinCount(gameViewModel: GameViewModel, modifier: Modifier) {
    Row(
        modifier = modifier
    ) {
        for (i in 0 until gameViewModel.defaultCoinCount) {
            if (i < gameViewModel.coinsForPlaying) {
                Image(
                    painter = painterResource(id = R.drawable.coin_tomato),
                    contentDescription = "coin",
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            } else {
                // grayscale 처리: 채도를 0으로
                Image(
                    painter = painterResource(id = R.drawable.coin_tomato),
                    contentDescription = "coin",
                    colorFilter = ColorFilter.colorMatrix(ColorMatrix().apply {
                        setToSaturation(
                            0.2f
                        )
                    }),
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun BestScore(context: Context, modifier: Modifier) {
    val db = remember { MyDb.getDatabase(context) }
    val list by db.userDao().getAll().collectAsStateWithLifecycle(emptyList())
    val maxScore = list.maxOfOrNull { it.score ?: 0 } ?: 0

    Box(modifier = modifier) {
        // 아웃라인 역할 텍스트 (여러 오프셋)
        Text(
            text = maxScore.toString(),
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = (-2).dp, y = (-2).dp),
            style = TextStyle(
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF003707),
                fontSize = 60.sp
            )
        )
        Text(
            text = maxScore.toString(),
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = (2).dp, y = (-2).dp),
            style = TextStyle(
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF003707),
                fontSize = 60.sp
            )
        )
        Text(
            text = maxScore.toString(),
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = (-2).dp, y = (2).dp),
            style = TextStyle(
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF003707),
                fontSize = 60.sp
            )
        )
        Text(
            text = maxScore.toString(),
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = (2).dp, y = (2).dp),
            style = TextStyle(
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF003707),
                fontSize = 60.sp
            )
        )
        // 본문 텍스트
        Text(
            text = maxScore.toString(),
            modifier = Modifier.align(Alignment.Center),
            style = TextStyle(
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFFF00E100),
                fontSize = 60.sp
            )
        )
    }
}

@Composable
fun rememberVibrate(): () -> Unit {
    val context = LocalContext.current
    // context가 변경되지 않도록 remember로 묶어줍니다.
    val vibrator = remember(context) {
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }
    return {
        val vibrationEffect = VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)
        vibrator.vibrate(vibrationEffect)
    }
}