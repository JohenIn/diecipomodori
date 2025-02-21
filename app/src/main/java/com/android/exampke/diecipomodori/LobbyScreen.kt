package com.android.exampke.diecipomodori

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.android.exampke.diecipomodori.model.MyDb
import com.android.exampke.diecipomodori.viewmodel.GameViewModel

@Composable
fun LobbyScreen(navController: NavController, gameViewModel: GameViewModel) {
    // 예시: total coin 개수를 3개로 가정
    val totalCoins = 3
    // playCount가 2라면, missingCoin = totalCoins - playCount = 1 -> 1 coin grayscale, 나머지 원본
    val missingCoins = totalCoins - gameViewModel.defaultCoinCount
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // BoxWithConstraints의 제약조건을 사용하여 화면 크기를 변수에 할당
        val screenWidth = maxWidth
        val screenHeight = maxHeight
        val context = LocalContext.current
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val vibrate: () -> Unit = {
            val vibrationEffect =
                VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)
            vibrator.vibrate(vibrationEffect)
        }

        // 배경 이미지 (전체 화면)
        Image(
            painter = painterResource(id = R.drawable.lobby_backgroundsvg),
            contentDescription = "tomato",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Play 버튼 이미지 (중앙)
        Image(
            painter = painterResource(id = R.drawable.board_playbutton),
            contentDescription = "tomato",
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (-screenHeight * (0.18f)))
                .clickable {
                    if (gameViewModel.coinsForPlaying > 0) {
                        gameViewModel.increaseUsedCoin()
                        navController.navigate("game")
                        vibrate()
                    } else {
                        vibrate()
                    }
                }
        )

        Row(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(top = 30.dp)
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
                                0f
                            )
                        }),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }
        }


        // 상단 로고: 화면 너비의 70%
        Image(
            painter = painterResource(id = R.drawable.mainlobbylogo),
            contentDescription = "tomato",
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .align(Alignment.TopCenter)
                .padding(top = screenHeight * 0.05f) // 예시: 상단 패딩 10% 사용
        )

        // 하단 오른쪽 베스트 스코어 영역
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .width(screenWidth * (0.3f))
                .padding(end = screenWidth * (0.05f))
        ) {
            Image(
                painter = painterResource(id = R.drawable.board_bestscore),
                contentDescription = "tomato",
                modifier = Modifier
            )
            val db = remember { MyDb.getDatabase(context) }
            val list by db.userDao().getAll().collectAsStateWithLifecycle(emptyList())
            val maxScore = list.maxOfOrNull { it.score ?: 0 } ?: 0

            Box(modifier = Modifier.align(Alignment.Center)) {
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
    }
}
