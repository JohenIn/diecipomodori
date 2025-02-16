package com.android.exampke.diecipomodori

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.zIndex
import com.android.exampke.diecipomodori.ui.theme.DieciPomodoriTheme
import kotlinx.coroutines.delay
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 전체 화면 모드 설정 (상태바, 네비게이션바 숨김)
        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = androidx.core.view.WindowInsetsControllerCompat(window, window.decorView)
        controller.systemBarsBehavior =
            androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller.hide(androidx.core.view.WindowInsetsCompat.Type.systemBars())
        enableEdgeToEdge()
        setContent {
            DieciPomodoriTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    GameScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun GameScreen(modifier: Modifier = Modifier) {
    // 인트로 화면
    var gameStarted by remember { mutableStateOf(false) }
    if (!gameStarted) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "TOMATOMATOMATO",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { gameStarted = true }) {
                    Text(text = "Start")
                }
            }
        }
    } else {
        // 게임 진행 상태 (restartTrigger가 변경되면 상태 재초기화됨)
        var restartTrigger by remember { mutableStateOf(0) }
        val numRows = 10
        val numCols = 17

        // 10×17 격자: 각 셀에 1~9 난수를 할당 (합이 10이면 null로 변경)
        val board = remember(restartTrigger) {
            mutableStateListOf<MutableList<Int?>>().apply {
                repeat(numRows) {
                    add(MutableList(numCols) { Random.nextInt(1, 10) })
                }
            }
        }
        var score by remember(restartTrigger) { mutableStateOf(0) }
        var timeLeft by remember(restartTrigger) { mutableStateOf(120) }
        // 셀 스냅 선택 (선택된 셀 인덱스)
        var dragStartCell by remember(restartTrigger) { mutableStateOf<Pair<Int, Int>?>(null) }
        var dragCurrentCell by remember(restartTrigger) { mutableStateOf<Pair<Int, Int>?>(null) }
        // 셀의 좌표를 그리드 컨테이너의 로컬 좌표계로 저장 (boundsInParent 사용)
        val cellBounds = remember(restartTrigger) { mutableStateMapOf<Pair<Int, Int>, Rect>() }
        // 자유 드래그 raw offset (pointerInput 이벤트가 그리드 컨테이너의 로컬 좌표계를 사용)
        var freeDragStartOffset by remember { mutableStateOf<Offset?>(null) }
        var freeDragCurrentOffset by remember { mutableStateOf<Offset?>(null) }

        // 120초 타이머
        LaunchedEffect(restartTrigger) {
            while (timeLeft > 0) {
                delay(1000L)
                timeLeft--
            }
        }

        // 전체 UI를 Row로 구성: 왼쪽에 게임 그리드, 오른쪽에 사이드바
        Row(modifier = Modifier.fillMaxSize()) {
            // 게임 그리드 영역 (왼쪽, weight 1)
            Spacer(modifier =Modifier.weight(1f))
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(vertical = 10.dp)
            ) {
                val availableWidth = maxWidth
                val availableHeight = maxHeight
                // 각 셀의 크기는 availableWidth/numCols, availableHeight/numRows 중 작은 값
                val cellSize = min(availableWidth / numCols, availableHeight / numRows)
                val gridWidth = cellSize * numCols
                val gridHeight = cellSize * numRows

                // 그리드 컨테이너 (pointerInput은 이 Box에 직접 적용 → 로컬 좌표계 사용)
                Box(
                    modifier = Modifier
                        .size(gridWidth, gridHeight)
                        .align(Alignment.Center)
                        .pointerInput(restartTrigger) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    if (timeLeft <= 0) return@detectDragGestures
                                    // 셀 스냅: 터치 좌표를 cellSize.toPx()로 나누어 인덱스 계산
                                    val col = (offset.x / cellSize.toPx()).toInt().coerceIn(0, numCols - 1)
                                    val row = (offset.y / cellSize.toPx()).toInt().coerceIn(0, numRows - 1)
                                    dragStartCell = Pair(row, col)
                                    dragCurrentCell = dragStartCell
                                    // 자유 드래그 raw offset 저장 (로컬 좌표 그대로)
                                    freeDragStartOffset = offset
                                    freeDragCurrentOffset = offset
                                },
                                onDrag = { change, _ ->
                                    if (timeLeft <= 0) return@detectDragGestures
                                    val col = (change.position.x / cellSize.toPx()).toInt().coerceIn(0, numCols - 1)
                                    val row = (change.position.y / cellSize.toPx()).toInt().coerceIn(0, numRows - 1)
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
                        .onGloballyPositioned { /* pointerInput 이미 로컬 좌표 사용 */ }
                ) {
                    // 셀 배치: 각 셀의 좌표는 boundsInParent()를 사용해 로컬 좌표계로 저장
                    Column {
                        for (rowIndex in 0 until numRows) {
                            Row {
                                for (colIndex in 0 until numCols) {
                                    if (board[rowIndex][colIndex] != null) {
                                        val isSelected = if (dragStartCell != null && dragCurrentCell != null) {
                                            val minRow = min(dragStartCell!!.first, dragCurrentCell!!.first)
                                            val maxRow = max(dragStartCell!!.first, dragCurrentCell!!.first)
                                            val minCol = min(dragStartCell!!.second, dragCurrentCell!!.second)
                                            val maxCol = max(dragStartCell!!.second, dragCurrentCell!!.second)
                                            rowIndex in minRow..maxRow && colIndex in minCol..maxCol
                                        } else false
                                        val imageAlpha = if (isSelected) 1f else 0.5f

                                        Box(
                                            modifier = Modifier
                                                .size(cellSize)
                                                .padding(1.dp)
                                                // border 제거함
                                                .onGloballyPositioned { coords ->
                                                    cellBounds[rowIndex to colIndex] = coords.boundsInParent()
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Image(
                                                painter = painterResource(id = R.drawable.numbertomato),
                                                contentDescription = "tomato",
                                                alpha = imageAlpha,
                                            )
                                            Text(
                                                text = board[rowIndex][colIndex].toString(),
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                            )
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.size(cellSize))
                                    }
                                }
                            }
                        }
                    }
                    // 빨간색 자유 드래그 선택 영역 Canvas: pointerInput의 raw offset 그대로 사용
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
            // 우측 사이드바: 점수와 시간, 그리고 세로 진행률 바를 Column으로 배치 (시간 텍스트 아래)
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(120.dp)
                    .padding(top = 20.dp)
                    .weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "점수: $score",
                    style = MaterialTheme.typography.bodyLarge,
                    fontSize = 20.sp
                )
                Text(
                    text = "시간: ${timeLeft}s",
                    style = MaterialTheme.typography.bodyLarge,
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.height(20.dp))
                val animatedProgress by animateFloatAsState(
                    targetValue = (120 - timeLeft) / 120f,
                    animationSpec = tween(durationMillis = 1000, easing = LinearEasing), label = ""
                )
                VerticalProgressBar(
                    progress = animatedProgress,
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(bottom = 10.dp)
                )
            }
        }
        // 게임 종료 오버레이
        if (timeLeft <= 0) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xAA000000)),
                contentAlignment = Alignment.Center
            ) {
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
                    Button(onClick = { restartTrigger++ }) {
                        Text(text = "Restart")
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