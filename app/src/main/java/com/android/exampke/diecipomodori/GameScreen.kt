package com.android.exampke.diecipomodori

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.android.exampke.diecipomodori.model.MyDb
import com.android.exampke.diecipomodori.model.User
import com.android.exampke.diecipomodori.viewmodel.GameViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

@Composable
fun GameScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    gameViewModel: GameViewModel
) {
    // 인트로 화면 및 게임 시작 상태
    var gameStarted by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(true) }
    // 일시정지 상태 변수
    var isPaused by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    val vibrate: () -> Unit = {
        val vibrationEffect =
            VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)
        vibrator.vibrate(vibrationEffect)
    }
    if (!gameStarted) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.drawable.playbackground),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(-1f)
                    .alpha(0.5f)
            )
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
                        }
                        .background(Color.Transparent)
                )
            }
        }
    } else {
        // 게임 진행 상태
        Image(
            painter = painterResource(R.drawable.playbackground),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .zIndex(-1f)
        )
        var restartTrigger by remember { mutableStateOf(0) }
        val numRows = 10
        val numCols = 17
        val board = remember(restartTrigger) {
            mutableStateListOf<MutableList<Int?>>().apply {
                repeat(numRows) {
                    add(MutableList(numCols) { Random.nextInt(1, 10) })
                }
            }
        }
        var score by remember(restartTrigger) { mutableStateOf(0) }
        var timeLeft by remember(restartTrigger) { mutableStateOf(120) } // 테스트 후 120초로 변경 시간 설정 변수
        var dragStartCell by remember(restartTrigger) { mutableStateOf<Pair<Int, Int>?>(null) }
        var dragCurrentCell by remember(restartTrigger) { mutableStateOf<Pair<Int, Int>?>(null) }
        val cellBounds = remember(restartTrigger) { mutableStateMapOf<Pair<Int, Int>, Rect>() }
        var freeDragStartOffset by remember { mutableStateOf<Offset?>(null) }
        var freeDragCurrentOffset by remember { mutableStateOf<Offset?>(null) }

        // 타이머: 일시정지 상태에서는 업데이트를 잠시 멈춤
        LaunchedEffect(restartTrigger, isPaused) {
            while (timeLeft > 0) {
                if (!isPaused) {
                    delay(1000L)
                    timeLeft--
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
                                        timeLeft = 120
                                        // 기타 초기화 작업이 필요하다면 추가
                                        isPaused = false
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
                                    vibrate()
                                }
                                .background(Color.Transparent)
                        )
                    }
                    Row(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(bottom = 30.dp)
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
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xAA000000)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.button_backhomeaftergame),
                        contentDescription = null,
                        modifier = Modifier
                            .clickable {
                                navController.navigate("lobby")
                                vibrate()
                            }
                            .align(Alignment.TopStart)
                    )
                    Image(
                        painter = painterResource(R.drawable.button_replay),
                        contentDescription = null,
                        modifier = Modifier
                            .clickable {
                                restartTrigger++
                                vibrate()
                            }
                            .align(Alignment.TopEnd)
                    )
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
                    }
                }
            }
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(vertical = 10.dp)
            ) {
                val availableWidth = maxWidth
                val availableHeight = maxHeight
                val cellSize = min(availableWidth / numCols, availableHeight / numRows)
                val gridWidth = cellSize * numCols
                val gridHeight = cellSize * numRows

                Box(
                    modifier = Modifier
                        .size(gridWidth, gridHeight)
                        .align(Alignment.Center)
                        .pointerInput(restartTrigger) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    if (timeLeft <= 0) return@detectDragGestures
                                    val col = (offset.x / cellSize.toPx())
                                        .toInt()
                                        .coerceIn(0, numCols - 1)
                                    val row = (offset.y / cellSize.toPx())
                                        .toInt()
                                        .coerceIn(0, numRows - 1)
                                    dragStartCell = Pair(row, col)
                                    dragCurrentCell = dragStartCell
                                    freeDragStartOffset = offset
                                    freeDragCurrentOffset = offset
                                },
                                onDrag = { change, _ ->
                                    if (timeLeft <= 0) return@detectDragGestures
                                    val col = (change.position.x / cellSize.toPx())
                                        .toInt()
                                        .coerceIn(0, numCols - 1)
                                    val row = (change.position.y / cellSize.toPx())
                                        .toInt()
                                        .coerceIn(0, numRows - 1)
                                    dragCurrentCell = Pair(row, col)
                                    freeDragCurrentOffset = change.position
                                },
                                onDragEnd = {
                                    if (timeLeft <= 0) return@detectDragGestures
                                    if (dragStartCell != null && dragCurrentCell != null) {
                                        val (startRow, startCol) = dragStartCell!!
                                        val (endRow, endCol) = dragCurrentCell!!
                                        val minRow = min(startRow, endRow)
                                        val maxRow = max(startRow, endRow)
                                        val minCol = min(startCol, endCol)
                                        val maxCol = max(startCol, endCol)
                                        val sum = (minRow..maxRow).sumOf { row ->
                                            (minCol..maxCol).sumOf { col ->
                                                board[row][col] ?: 0
                                            }
                                        }
                                        if (sum == 10) {
                                            for (row in minRow..maxRow) {
                                                for (col in minCol..maxCol) {
                                                    if (board[row][col] != null) {
                                                        board[row][col] = null
                                                        score++
                                                        vibrate()
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    dragStartCell = null
                                    dragCurrentCell = null
                                    freeDragStartOffset = null
                                    freeDragCurrentOffset = null
                                }
                            )
                        }
                        .onGloballyPositioned { /* 이미 로컬 좌표 사용 */ }
                ) {
                    Column {
                        for (rowIndex in 0 until numRows) {
                            Row {
                                for (colIndex in 0 until numCols) {
                                    if (board[rowIndex][colIndex] != null) {
                                        val isSelected =
                                            if (dragStartCell != null && dragCurrentCell != null) {
                                                val minRow = min(
                                                    dragStartCell!!.first,
                                                    dragCurrentCell!!.first
                                                )
                                                val maxRow = max(
                                                    dragStartCell!!.first,
                                                    dragCurrentCell!!.first
                                                )
                                                val minCol = min(
                                                    dragStartCell!!.second,
                                                    dragCurrentCell!!.second
                                                )
                                                val maxCol = max(
                                                    dragStartCell!!.second,
                                                    dragCurrentCell!!.second
                                                )
                                                rowIndex in minRow..maxRow && colIndex in minCol..maxCol
                                            } else false
                                        val imageAlpha = if (isSelected) 1f else 0.5f

                                        Box(
                                            modifier = Modifier
                                                .size(cellSize)
                                                .padding(1.dp)
                                                .onGloballyPositioned { coords ->
                                                    cellBounds[rowIndex to colIndex] =
                                                        coords.boundsInParent()
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Image(
                                                painter = painterResource(id = R.drawable.numbertomato),
                                                contentDescription = "tomato",
                                                alpha = imageAlpha
                                            )
                                            Text(
                                                text = board[rowIndex][colIndex].toString(),
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.size(cellSize))
                                    }
                                }
                            }
                        }
                    }
                    if (freeDragStartOffset != null && freeDragCurrentOffset != null) {
                        val topLeft = Offset(
                            x = min(freeDragStartOffset!!.x, freeDragCurrentOffset!!.x),
                            y = min(freeDragStartOffset!!.y, freeDragCurrentOffset!!.y)
                        )
                        val bottomRight = Offset(
                            x = max(freeDragStartOffset!!.x, freeDragCurrentOffset!!.x),
                            y = max(freeDragStartOffset!!.y, freeDragCurrentOffset!!.y)
                        )
                        Canvas(modifier = Modifier.matchParentSize()) {
                            drawRect(
                                color = Color.Yellow,
                                topLeft = topLeft,
                                size = Size(
                                    width = bottomRight.x - topLeft.x,
                                    height = bottomRight.y - topLeft.y
                                ),
                                style = Stroke(width = 3.dp.toPx())
                            )
                        }
                    }
                }
            }
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
                        text = "${timeLeft}s",
                        style = MaterialTheme.typography.bodyLarge,
                        fontSize = 18.sp,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                val animatedProgress by animateFloatAsState(
                    targetValue = (120 - timeLeft) / 120f,
                    animationSpec = tween(durationMillis = 1000, easing = LinearEasing),
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
        LaunchedEffect(timeLeft) {
            if (timeLeft <= 0) {
                isPlaying = false
            }
        }
        val db = remember { MyDb.getDatabase(context) }
        LaunchedEffect(isPlaying) {
            if (!isPlaying) {
                withContext(Dispatchers.IO) {
                    db.userDao().insertIfHigher(User(score = score))
                }
            }
        }
        // 게임 종료 오버레이 (if timeLeft <= 0)
        if (timeLeft <= 0) {
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
                        painter = painterResource(R.drawable.button_replay),
                        contentDescription = "replay",
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
                    Row(
                        modifier = Modifier
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
                                            0.2f
                                        )
                                    }),
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )
                            }
                        }
                    }
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