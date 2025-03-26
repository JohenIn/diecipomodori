package com.android.exampke.diecipomodori

import android.media.MediaPlayer
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.android.exampke.diecipomodori.model.MyDb
import com.android.exampke.diecipomodori.model.User
import com.android.exampke.diecipomodori.viewmodel.GameViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@Composable
fun GameScreen(
    navController: NavController,
    gameViewModel: GameViewModel
) {
    // 인트로 화면 및 게임 시작 상태
    val context = LocalContext.current
    var gameStarted by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }
    // 일시정지 상태 변수
    var isPaused by remember { mutableStateOf(false) }
    val vibrate = rememberVibrate()

    // MediaPlayer 인스턴스를 상태로 관리
    var bgmPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

// LifecycleEventObserver를 사용하여 onPause 이벤트 감지
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                // 앱이 백그라운드로 전환되거나 화면이 꺼지면 자동으로 pause 처리
                isPaused = true
                // 필요하다면 추가적인 pause 처리 로직을 넣을 수 있습니다.
                bgmPlayer?.takeIf { it.isPlaying }?.pause()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }


    if (!gameStarted) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            ScreenWallpaper(R.drawable.game_wallpaper,modifier = Modifier.alpha(0.5f))
            BoxWithConstraints(modifier = Modifier.align(Alignment.TopCenter)) {
                // 이미지의 하단 20% 영역에 클릭 가능 오버레이 추가
                Image(
                    painter = painterResource(R.drawable.startbutton),
                    contentDescription = null,
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .width(maxWidth * 0.2f)
                        .height(maxHeight * 0.2f)
                        .clickable {
                            gameViewModel.increaseUsedCoin()
                            vibrate()
                            gameStarted = true
                            isPlaying = true
                            isPaused = false
                            // BGM 생성 및 재생 (처음부터 시작)
                            bgmPlayer?.release() // 혹시 기존 인스턴스가 있다면 해제
                            bgmPlayer = MediaPlayer.create(context, R.raw.gameplayingbgm).apply {
                                isLooping = false
                                setVolume(0.05f, 0.05f)
                                start()}
                        }
                        .background(Color.Transparent)
                )
            }
        }
    } else {
        // 게임 진행 상태
        ScreenWallpaper(R.drawable.game_wallpaper)
        var restartTrigger by remember { mutableStateOf(0) }
        var score by remember(restartTrigger) { mutableStateOf(0) }
        var totalSeconds by remember(restartTrigger) { mutableStateOf(120) }// 테스트 후 120초로 변경 시간 설정 변수
        var leftSeconds by remember(restartTrigger) { mutableStateOf(totalSeconds) }

        // 타이머: 일시정지 상태에서는 업데이트를 잠시 멈춤
        LaunchedEffect(restartTrigger, isPaused) {
            while (leftSeconds > 0) {
                if (!isPaused) {
                    delay(1000L)
                    leftSeconds--
                } else {
                    delay(100L)
                }
            }
        }
        // 전체 UI를 Row로 구성: 왼쪽은 게임 그리드, 오른쪽은 사이드바
        Row(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(R.drawable.button_pause),
                contentDescription = "pause",
                modifier = Modifier
                    .clickable {
                        isPaused = true
                        // BGM 일시정지
                        bgmPlayer?.takeIf { it.isPlaying }?.pause()
                        vibrate()
                    }
                    .weight(1f)
            )
            // Pause 오버레이: 화면 좌측 상단의 Pause 버튼을 누르면 나타남
            if (isPaused) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFFFFFFF).copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    BoxWithConstraints(modifier = Modifier.align(Alignment.TopStart)) {
                        // 이미지의 하단 20% 영역에 클릭 가능 오버레이 추가
                        Image(
                            painter = painterResource(R.drawable.button_backhomeaftergame),
                            contentDescription = "lobby",
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .width(maxWidth * 0.2f)
                                .height(maxHeight * 0.2f)
                                .clickable {
                                    navController.navigate("lobby")
                                    vibrate()
                                }
                                .background(Color.Transparent)
                        )
                    }
                    BoxWithConstraints(modifier = Modifier.align(Alignment.TopCenter)) {
                        // 이미지의 하단 20% 영역에 클릭 가능 오버레이 추가
                        Image(
                            painter = painterResource(R.drawable.button_reset),
                            contentDescription = "reset",
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .width(maxWidth * 0.2f)
                                .height(maxHeight * 0.2f)
                                .clickable {
                                    if (gameViewModel.coinsForPlaying > 0) {
                                        gameViewModel.increaseUsedCoin()
                                        restartTrigger++
                                        score = 0
                                        // 기타 초기화 작업이 필요하다면 추가
                                        isPaused = false
                                        // Reset: 기존 BGM 중지 후 새로 시작
                                        bgmPlayer?.let { player ->
                                            if (player.isPlaying) {
                                                player.stop()
                                            }
                                            player.release()
                                        }
                                        bgmPlayer = MediaPlayer.create(context, R.raw.gameplayingbgm).apply {
                                            isLooping = false
                                            setVolume(0.3f, 0.3f)
                                            start()
                                        }
                                        vibrate()
                                    } else {
                                        vibrate()
                                    }
                                }
                                .background(Color.Transparent)
                        )
                    }
                    BoxWithConstraints(modifier = Modifier.align(Alignment.TopEnd)) {
                        // 이미지의 하단 20% 영역에 클릭 가능 오버레이 추가
                        Image(
                            painter = painterResource(R.drawable.button_resume),
                            contentDescription = "resume",
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .width(maxWidth * 0.2f)
                                .height(maxHeight * 0.2f)
                                .clickable {
                                    isPaused = false
                                    // 일시정지된 위치에서 재개
                                    bgmPlayer?.start()
                                    vibrate()
                                }
                                .background(Color.Transparent)
                        )
                    }
                    TomatoCoinCount(
                        gameViewModel,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(bottom = 30.dp)
                    )
                }
            }
            GameField(restartTrigger, leftSeconds, score, onScoreChange = { newScore ->
                score = newScore
            })

            // 우측 사이드바: 점수, 시간, 진행률 및 시계 배경을 Column으로 배치
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(120.dp)
                    .weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box {
                    Image(
                        painter = painterResource(id = R.drawable.scoreboard),
                        contentDescription = "Score background",
                        modifier = Modifier.size(120.dp)
                    )
                    Text(
                        text = score.toString(),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .offset(x = (-20).dp)
                    )
                }
                Box {
                    Image(
                        painter = painterResource(id = R.drawable.clock),
                        contentDescription = "Clock background",
                        modifier = Modifier.size(80.dp)
                    )
                    Text(
                        text = "${leftSeconds}s",
                        style = MaterialTheme.typography.bodyLarge,
                        fontSize = 18.sp,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                val animatedProgress by animateFloatAsState(
                    targetValue = (totalSeconds - leftSeconds) / totalSeconds.toFloat(),
                    animationSpec = if (leftSeconds == totalSeconds) {
                        tween(durationMillis = 0) // 초기 상태에서는 즉시 0으로 설정
                    } else {
                        tween(durationMillis = 1000, easing = LinearEasing)
                    },
                    label = ""
                )
                VerticalProgressBar(
                    progress = animatedProgress,
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(bottom = 10.dp)
                )
            }
        }
        LaunchedEffect(leftSeconds) {
            if (leftSeconds <= 0) {
                isPlaying = false
            }
        }
        val db = remember { MyDb.getDatabase(context) }
        LaunchedEffect(isPlaying) {
            if (!isPlaying) {
                withContext(Dispatchers.IO) {
                    db.userDao().insertIfHigher(User(score = score))
                }
                bgmPlayer?.let { player ->
                    if (player.isPlaying) {
                        player.stop()
                    }
                    player.release()
                }
                bgmPlayer = null
            }
        }
        // 게임 종료 오버레이 (if timeLeft <= 0)
        if (leftSeconds <= 0) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xAA000000)),
                contentAlignment = Alignment.Center
            ) {
                BoxWithConstraints(modifier = Modifier.align(Alignment.TopStart)) {
                    // 이미지의 하단 20% 영역에 클릭 가능 오버레이 추가
                    Image(
                        painter = painterResource(R.drawable.button_backhomeaftergame),
                        contentDescription = "lobby",
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .width(maxWidth * 0.2f)
                            .height(maxHeight * 0.2f)
                            .clickable {
                                navController.navigate("lobby")
                                vibrate()
                            }
                            .background(Color.Transparent)
                    )
                }
                BoxWithConstraints(modifier = Modifier.align(Alignment.TopEnd)) {
                    // 이미지의 하단 20% 영역에 클릭 가능 오버레이 추가
                    Image(
                        painter = painterResource(R.drawable.button_playagain),
                        contentDescription = "play again",
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .width(maxWidth * 0.2f)
                            .height(maxHeight * 0.2f)
                            .clickable {
                                if (gameViewModel.coinsForPlaying > 0) {
                                    gameViewModel.increaseUsedCoin()
                                    restartTrigger++
                                    score = 0
                                    isPlaying = true
                                    // 기존 BGM 중지 및 새로 시작
                                    bgmPlayer?.let { player ->
                                        if (player.isPlaying) {
                                            player.stop()
                                        }
                                        player.release()
                                    }
                                    bgmPlayer = MediaPlayer.create(context, R.raw.gameplayingbgm).apply {
                                        isLooping = false
                                        setVolume(0.3f, 0.3f)
                                        start()
                                    }
                                    vibrate()
                                } else {
                                    vibrate()
                                }
                            }
                            .background(Color.Transparent)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Game Finished",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Score: $score",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    TomatoCoinCount(gameViewModel, modifier = Modifier.padding(top = 30.dp))
                }
            }
        }
    }
}

@Composable
fun VerticalProgressBar(
    progress: Float, // 0f ~ 1f
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xFFFFC7C7),
    progressColor: Color = Color(0xFFDB3E3E)
) {
    Box(modifier = modifier.background(backgroundColor)) {
        Box(
            modifier = Modifier
                .width(20.dp)
                .fillMaxHeight(progress)
                .background(progressColor)
                .align(Alignment.BottomCenter)
        )
    }
}