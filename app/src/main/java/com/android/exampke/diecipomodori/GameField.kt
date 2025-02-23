package com.android.exampke.diecipomodori

import android.media.MediaPlayer
import android.media.SoundPool
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

@Composable
fun GameField(
    restartTrigger: Int,
    timeLeft: Int,
    score: Int,
    onScoreChange: (Int) -> Unit
) {
    val context = LocalContext.current

    val numRows = 10
    val numCols = 17
    val board = remember(restartTrigger) {
        mutableStateListOf<MutableList<Int?>>().apply {
            repeat(numRows) {
                add(MutableList(numCols) { Random.nextInt(1, 10) })
            }
        }
    }
    var dragStartCell by remember(restartTrigger) { mutableStateOf<Pair<Int, Int>?>(null) }
    var dragCurrentCell by remember(restartTrigger) { mutableStateOf<Pair<Int, Int>?>(null) }
    val cellBounds = remember(restartTrigger) { mutableStateMapOf<Pair<Int, Int>, Rect>() }
    var freeDragStartOffset by remember { mutableStateOf<Offset?>(null) }
    var freeDragCurrentOffset by remember { mutableStateOf<Offset?>(null) }
    val vibrate = rememberVibrate()

    //효과음 재생 여부
    var shouldPlaySound by remember { mutableStateOf(false) }
    LaunchedEffect(shouldPlaySound) {
        if (shouldPlaySound) {
            val mediaPlayer = MediaPlayer.create(context, R.raw.pop)
            mediaPlayer.start()
            delay(mediaPlayer.duration.toLong()) // 소리가 끝날 때까지 대기
            mediaPlayer.release() // 메모리 정리
            shouldPlaySound = false
        }
    }

    // SoundPool 초기화 및 사운드 로드 (컴포지션 시작 시 한 번만 실행)
    val soundPool = remember {
        SoundPool.Builder()
            .setMaxStreams(5)
            .build()
    }
    val popSoundId = remember {
        soundPool.load(context, R.raw.pop, 1)
    }


    BoxWithConstraints(
        modifier = Modifier
            .fillMaxHeight()
            .padding(vertical = 10.dp)
    ) {
        var pointAsScore by remember(restartTrigger) { mutableStateOf(score) }
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
                                                pointAsScore++
                                                onScoreChange(pointAsScore)
                                                vibrate()
                                            }
                                        }
                                    }
                                    //pop.ogg 효과음 재생 true
                                    //shouldPlaySound = true //효과음 재생 트리거 활성화

                                    // 효과음 재생: 볼륨 0.5f, 우선순위 1, 반복 없음, 재생 속도 1.0f
                                    soundPool.play(popSoundId, 1f, 1f, 1, 0, 1.0f)
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
                                        fontSize = 20.sp,
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
}